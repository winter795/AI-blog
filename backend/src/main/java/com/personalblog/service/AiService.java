package com.personalblog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalblog.config.AiConfig;
import com.personalblog.entity.Article;
import com.personalblog.mapper.ArticleMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class AiService {

    private final AiConfig config;
    private final ArticleMapper articleMapper;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(60)).build();

    // TF-IDF 词汇表缓存
    private volatile List<String> vocabulary = List.of();
    private volatile Map<Long, double[]> tfidfCache = Map.of();

    public AiService(AiConfig config, ArticleMapper articleMapper) {
        this.config = config;
        this.articleMapper = articleMapper;
    }

    // ── API 调用 ──
    private Map<String, Object> chatRequest(String systemPrompt, String userMessage, boolean stream) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", config.getModel());
        body.put("stream", stream);
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", userMessage));
        body.put("messages", messages);
        body.put("temperature", 0.7);
        return body;
    }

    private String callChatApi(Map<String, Object> body) throws IOException, InterruptedException {
        String json = mapper.writeValueAsString(body);
        var req = HttpRequest.newBuilder()
                .uri(URI.create(config.getBaseUrl() + "/chat/completions"))
                .header("Authorization", "Bearer " + config.getApiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        var resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) throw new RuntimeException("DeepSeek API error: " + resp.body());
        return resp.body();
    }

    // ── 本地 TF-IDF 向量化 ──
    private synchronized void buildVocabulary() {
        List<Article> articles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>().eq(Article::getStatus, 1));
        Set<String> wordSet = new LinkedHashSet<>();
        for (Article a : articles) {
            for (String w : tokenize(a.getTitle() + " " + (a.getSummary() != null ? a.getSummary() : ""))) {
                wordSet.add(w);
            }
        }
        vocabulary = new ArrayList<>(wordSet);
        Map<Long, double[]> cache = new HashMap<>();
        int N = articles.size();
        Map<String, Integer> df = new HashMap<>();
        for (String w : vocabulary) {
            int count = 0;
            for (Article a : articles) {
                String text = a.getTitle() + " " + (a.getSummary() != null ? a.getSummary() : "");
                if (tokenize(text).contains(w)) count++;
            }
            df.put(w, count);
        }
        for (Article a : articles) {
            String text = a.getTitle() + " " + (a.getSummary() != null ? a.getSummary() : "");
            List<String> tokens = tokenize(text);
            double[] vec = new double[vocabulary.size()];
            Map<String, Integer> tf = new HashMap<>();
            for (String t : tokens) tf.merge(t, 1, Integer::sum);
            for (int i = 0; i < vocabulary.size(); i++) {
                String w = vocabulary.get(i);
                double tfVal = tf.getOrDefault(w, 0) > 0 ? 1.0 + Math.log(tf.getOrDefault(w, 0)) : 0;
                double idf = Math.log((N + 1.0) / (df.getOrDefault(w, 1) + 1.0)) + 1.0;
                vec[i] = tfVal * idf;
            }
            cache.put(a.getId(), vec);
        }
        tfidfCache = cache;
    }

    private double[] queryVector(String query) {
        if (vocabulary.isEmpty()) buildVocabulary();
        List<String> tokens = tokenize(query);
        Map<String, Integer> tf = new HashMap<>();
        for (String t : tokens) tf.merge(t, 1, Integer::sum);
        double[] vec = new double[vocabulary.size()];
        for (int i = 0; i < vocabulary.size(); i++) {
            String w = vocabulary.get(i);
            vec[i] = tf.getOrDefault(w, 0) > 0 ? 1.0 + Math.log(tf.getOrDefault(w, 0)) : 0;
        }
        return vec;
    }

    private double cosine(double[] a, double[] b) {
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }
        return (na == 0 || nb == 0) ? 0 : dot / (Math.sqrt(na) * Math.sqrt(nb));
    }

    private List<String> tokenize(String text) {
        if (text == null) return List.of();
        // 中英文混合分词
        List<String> tokens = new ArrayList<>();
        // 中文按字符切，英文按空格切
        StringBuilder buf = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN) {
                if (buf.length() > 0) { tokens.add(buf.toString().toLowerCase()); buf.setLength(0); }
                tokens.add(String.valueOf(c));
            } else if (Character.isLetterOrDigit(c)) {
                buf.append(c);
            } else {
                if (buf.length() > 0) { tokens.add(buf.toString().toLowerCase()); buf.setLength(0); }
            }
        }
        if (buf.length() > 0) tokens.add(buf.toString().toLowerCase());
        return tokens.stream().filter(t -> t.length() >= 1).distinct().collect(Collectors.toList());
    }

    // ── 混合检索 ──
    public List<Map<String, Object>> hybridSearch(String query, int topK) {
        if (tfidfCache.isEmpty()) buildVocabulary();
        double[] queryVec = queryVector(query);
        List<Article> articles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>().eq(Article::getStatus, 1));

        // 关键词匹配
        Set<Long> keywordIds = articles.stream()
                .filter(a -> a.getTitle().contains(query) ||
                        (a.getSummary() != null && a.getSummary().contains(query)))
                .map(Article::getId).collect(Collectors.toSet());

        List<Map<String, Object>> scored = new ArrayList<>();
        for (Article a : articles) {
            double[] vec = tfidfCache.get(a.getId());
            if (vec == null) continue;
            double score = cosine(queryVec, vec);
            if (keywordIds.contains(a.getId())) score = Math.max(score, 0.5) + 0.25;
            if (score > 0.05) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", a.getId());
                m.put("title", a.getTitle());
                m.put("summary", a.getSummary());
                m.put("score", Math.round(score * 1000.0) / 1000.0);
                scored.add(m);
            }
        }
        scored.sort((x, y) -> Double.compare((double) y.get("score"), (double) x.get("score")));
        return scored.subList(0, Math.min(topK, scored.size()));
    }

    // ── SSE 流式问答 ──
    public SseEmitter chatStream(String query, List<Map<String, Object>> context) {
        SseEmitter emitter = new SseEmitter(120000L);
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                emitter.send(SseEmitter.event().name("sources").data(context));

                StringBuilder ctx = new StringBuilder();
                for (int i = 0; i < context.size(); i++) {
                    Map<String, Object> c = context.get(i);
                    ctx.append("[").append(i + 1).append("] ")
                       .append(c.get("title")).append(": ")
                       .append(c.get("summary") != null ? c.get("summary") : "").append("\n");
                }
                String system = "你是博客知识助手。根据以下文章回答用户问题，引用时标注文章编号如[1][2]。如果文章无相关信息，如实告知。\n\n文章:\n" + ctx;
                Map<String, Object> body = chatRequest(system, query, true);

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
                            if (choices != null && choices.size() > 0) {
                                var delta = choices.get(0).get("delta");
                                if (delta != null && delta.has("content")) {
                                    emitter.send(SseEmitter.event().name("token").data(delta.get("content").asText()));
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                }
                emitter.complete();
            } catch (Exception e) {
                try { emitter.send(SseEmitter.event().name("error").data(e.getMessage())); }
                catch (IOException ignored) {}
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    // ── AI 摘要 ──
    public String autoSummary(String content) throws Exception {
        String resp = callChatApi(chatRequest(
                "用1-2句话总结以下文章核心内容，不超过200字，直接输出摘要。",
                content, false));
        var node = mapper.readTree(resp);
        return node.get("choices").get(0).get("message").get("content").asText().trim();
    }

    // ── AI 标签 ──
    public List<String> autoTags(String content) throws Exception {
        String resp = callChatApi(chatRequest(
                "根据文章内容推荐3-5个标签，以JSON数组格式返回如[\"标签1\",\"标签2\"]，只返回JSON数组。",
                content, false));
        var node = mapper.readTree(resp);
        String text = node.get("choices").get(0).get("message").get("content").asText().trim();
        text = text.replaceAll("```json|```", "").trim();
        try {
            return mapper.readValue(text, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of(text);
        }
    }

    // ── 相关文章 ──
    public List<Map<String, Object>> relatedArticles(Long articleId, int n) {
        if (tfidfCache.isEmpty()) buildVocabulary();
        double[] targetVec = tfidfCache.get(articleId);
        if (targetVec == null) return List.of();
        List<Article> others = articleMapper.selectList(
                new LambdaQueryWrapper<Article>().eq(Article::getStatus, 1).ne(Article::getId, articleId));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Article a : others) {
            double[] vec = tfidfCache.get(a.getId());
            if (vec == null) continue;
            double score = cosine(targetVec, vec);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("title", a.getTitle());
            m.put("score", Math.round(score * 1000.0) / 1000.0);
            result.add(m);
        }
        result.sort((x, y) -> Double.compare((double) y.get("score"), (double) x.get("score")));
        return result.subList(0, Math.min(n, result.size()));
    }

    // ── 搜索建议 ──
    public List<String> suggest(String input) throws Exception {
        if (input == null || input.isBlank()) return List.of();
        String resp = callChatApi(chatRequest(
                "根据用户输入的部分问题推荐3个可能的完整问题，JSON数组返回如[\"问题1\",\"问题2\"]。只返回JSON。",
                input, false));
        var node = mapper.readTree(resp);
        String text = node.get("choices").get(0).get("message").get("content").asText().trim();
        text = text.replaceAll("```json|```", "").trim();
        try {
            return mapper.readValue(text, new TypeReference<List<String>>() {});
        } catch (Exception e) { return List.of(); }
    }

    // 刷新词汇表（文章变更后调用）
    public void refreshVocabulary() {
        vocabulary = List.of();
        tfidfCache = Map.of();
        buildVocabulary();
    }
}
