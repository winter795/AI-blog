package com.personalblog.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalblog.config.AiConfig;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

/**
 * AI 辅助写作：摘要生成、标签推荐、搜索建议。
 */
@Service
public class AiAssistantService {

    private final AiConfig config;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(60))
            .build();

    public AiAssistantService(AiConfig config) {
        this.config = config;
    }

    private Map<String, Object> chatRequest(String system, String user) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", config.getModel());
        body.put("stream", false);
        body.put("temperature", 0.7);
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", system));
        messages.add(Map.of("role", "user", "content", user));
        body.put("messages", messages);
        return body;
    }

    private String callApi(Map<String, Object> body) throws Exception {
        String json = mapper.writeValueAsString(body);
        var req = HttpRequest.newBuilder()
                .uri(URI.create(config.getBaseUrl() + "/chat/completions"))
                .header("Authorization", "Bearer " + config.getApiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        var resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) throw new RuntimeException("API error: " + resp.body());
        return resp.body();
    }

    private String extractContent(String resp) throws Exception {
        var node = mapper.readTree(resp);
        return node.get("choices").get(0).get("message").get("content").asText().trim();
    }

    /** AI 自动生成文章摘要。 */
    public String autoSummary(String content) throws Exception {
        String resp = callApi(chatRequest(
                "用1-2句话总结以下文章核心内容，不超过200字，直接输出摘要。", content));
        return extractContent(resp);
    }

    /** AI 自动推荐标签。 */
    public List<String> autoTags(String content) throws Exception {
        String resp = callApi(chatRequest(
                "根据文章内容推荐3-5个标签，以JSON数组格式返回如[\"标签1\",\"标签2\"]，只返回JSON数组。", content));
        String text = extractContent(resp);
        text = text.replaceAll("```json|```", "").trim();
        try {
            return mapper.readValue(text, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of(text);
        }
    }

    /** AI 搜索建议。 */
    public List<String> suggest(String input) throws Exception {
        if (input == null || input.isBlank()) return List.of();
        String resp = callApi(chatRequest(
                "根据用户输入的部分问题推荐3个可能的完整问题，JSON数组返回如[\"问题1\",\"问题2\"]。只返回JSON。",
                input));
        String text = extractContent(resp);
        text = text.replaceAll("```json|```", "").trim();
        try {
            return mapper.readValue(text, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
