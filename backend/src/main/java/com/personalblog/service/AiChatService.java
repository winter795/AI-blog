package com.personalblog.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalblog.config.AiConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * SSE 流式 AI 对话服务，负责 RAG 上下文构建和流式响应。
 */
@Service
public class AiChatService {

    private final AiConfig config;
    private final AiSearchService searchService;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(60))
            .build();

    public AiChatService(AiConfig config, AiSearchService searchService) {
        this.config = config;
        this.searchService = searchService;
    }

    /**
     * SSE 流式问答：检索相关文章 → 构建 RAG 上下文 → 流式输出答案。
     */
    public SseEmitter chatStream(String query) {
        SseEmitter emitter = new SseEmitter(120000L);

        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                List<Map<String, Object>> sources = searchService.hybridSearch(query, 5);
                emitter.send(SseEmitter.event().name("sources").data(sources));

                StringBuilder ctx = new StringBuilder();
                for (int i = 0; i < sources.size(); i++) {
                    Map<String, Object> c = sources.get(i);
                    ctx.append("[").append(i + 1).append("] ")
                            .append(c.get("title")).append("\n")
                            .append(c.get("snippet") != null ? c.get("snippet") : c.get("summary"))
                            .append("\n\n");
                }

                String system = """
                        你是博客知识助手。根据以下文章片段回答用户问题。
                        引用时标注编号如[1][2]。如果信息不足，如实告知。

                        文章片段:
                        """ + ctx;

                Map<String, Object> body = chatRequestBody(system, query, true);
                String json = mapper.writeValueAsString(body);

                var req = HttpRequest.newBuilder()
                        .uri(URI.create(config.getBaseUrl() + "/chat/completions"))
                        .header("Authorization", "Bearer " + config.getApiKey())
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                var resp = http.send(req, HttpResponse.BodyHandlers.ofLines());
                var linesIter = resp.body().iterator();
                while (linesIter.hasNext()) {
                    String line = linesIter.next();
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6);
                        if ("[DONE]".equals(data)) break;
                        try {
                            var node = mapper.readTree(data);
                            var choices = node.get("choices");
                            if (choices != null && !choices.isEmpty()) {
                                var delta = choices.get(0).get("delta");
                                if (delta != null && delta.has("content")) {
                                    emitter.send(SseEmitter.event()
                                            .name("token")
                                            .data(delta.get("content").asText()));
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                }
                emitter.complete();
            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event().name("error").data(e.getMessage()));
                } catch (IOException ignored) {}
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    private Map<String, Object> chatRequestBody(String system, String user, boolean stream) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", config.getModel());
        body.put("stream", stream);
        body.put("temperature", 0.7);
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", system));
        messages.add(Map.of("role", "user", "content", user));
        body.put("messages", messages);
        return body;
    }
}
