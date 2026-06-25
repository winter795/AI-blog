package com.personalblog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.personalblog.entity.Article;
import com.personalblog.mapper.ArticleMapper;
import com.personalblog.util.TokenizerUtil;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * 基于 jieba 分词 + TF-IDF 的文章检索引擎。
 * 索引全文（标题 + 摘要 + 正文），支持混合检索（向量 + 关键词）。
 */
@Service
public class AiSearchService {

    private final ArticleMapper articleMapper;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private volatile List<String> vocabulary = List.of();
    private volatile Map<Long, double[]> tfidfCache = Map.of();
    private volatile Map<Long, String> contentCache = Map.of();

    public AiSearchService(ArticleMapper articleMapper) {
        this.articleMapper = articleMapper;
    }

    // ── 索引构建 ──
    public void buildIndex() {
        lock.writeLock().lock();
        try {
            List<Article> articles = articleMapper.selectList(
                    new LambdaQueryWrapper<Article>().eq(Article::getStatus, 1));

            // 构建词汇表
            Set<String> wordSet = new LinkedHashSet<>();
            Map<Long, List<String>> articleTokens = new HashMap<>();
            for (Article a : articles) {
                String fullText = a.getTitle() + " " +
                        (a.getSummary() != null ? a.getSummary() : "") + " " +
                        (a.getContent() != null ? a.getContent() : "");
                List<String> tokens = TokenizerUtil.tokenize(fullText);
                articleTokens.put(a.getId(), tokens);
                wordSet.addAll(tokens);
            }
            vocabulary = new ArrayList<>(wordSet);

            // 计算 DF
            int N = articles.size();
            Map<String, Integer> df = new HashMap<>();
            for (String w : vocabulary) {
                int count = 0;
                for (Article a : articles) {
                    if (articleTokens.get(a.getId()).contains(w)) count++;
                }
                df.put(w, count);
            }

            // 计算 TF-IDF 向量
            Map<Long, double[]> cache = new HashMap<>();
            Map<Long, String> cc = new HashMap<>();
            for (Article a : articles) {
                List<String> tokens = articleTokens.get(a.getId());
                double[] vec = new double[vocabulary.size()];
                Map<String, Integer> tf = new HashMap<>();
                for (String t : tokens) tf.merge(t, 1, Integer::sum);
                for (int i = 0; i < vocabulary.size(); i++) {
                    String w = vocabulary.get(i);
                    double tfVal = tf.getOrDefault(w, 0) > 0
                            ? 1.0 + Math.log(tf.getOrDefault(w, 0)) : 0;
                    double idf = Math.log((N + 1.0) / (df.getOrDefault(w, 1) + 1.0)) + 1.0;
                    vec[i] = tfVal * idf;
                }
                cache.put(a.getId(), vec);
                cc.put(a.getId(), (a.getContent() != null ? a.getContent() : ""));
            }
            tfidfCache = cache;
            contentCache = cc;
        } finally {
            lock.writeLock().unlock();
        }
    }

    // ── 查询向量化 ──
    private double[] queryVector(String query) {
        lock.readLock().lock();
        try {
            if (vocabulary.isEmpty()) return new double[0];
            List<String> tokens = TokenizerUtil.tokenize(query);
            double[] vec = new double[vocabulary.size()];
            Map<String, Integer> tf = new HashMap<>();
            for (String t : tokens) tf.merge(t, 1, Integer::sum);
            for (int i = 0; i < vocabulary.size(); i++) {
                vec[i] = tf.getOrDefault(vocabulary.get(i), 0) > 0
                        ? 1.0 + Math.log(tf.getOrDefault(vocabulary.get(i), 0)) : 0;
            }
            return vec;
        } finally {
            lock.readLock().unlock();
        }
    }

    private double cosine(double[] a, double[] b) {
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }
        double denom = Math.sqrt(na) * Math.sqrt(nb);
        return denom == 0 ? 0 : dot / denom;
    }

    // ── 混合检索 ──
    public List<Map<String, Object>> hybridSearch(String query, int topK) {
        lock.readLock().lock();
        try {
            if (tfidfCache.isEmpty()) {
                lock.readLock().unlock();
                buildIndex();
                lock.readLock().lock();
            }

            double[] queryVec = queryVector(query);
            List<Article> articles = articleMapper.selectList(
                    new LambdaQueryWrapper<Article>().eq(Article::getStatus, 1));

            // 关键词匹配加分
            Set<Long> keywordIds = articles.stream()
                    .filter(a -> a.getTitle().contains(query) ||
                            (a.getSummary() != null && a.getSummary().contains(query)))
                    .map(Article::getId).collect(Collectors.toSet());

            List<Map<String, Object>> scored = new ArrayList<>();
            for (Article a : articles) {
                double[] vec = tfidfCache.get(a.getId());
                if (vec == null) continue;
                double score = cosine(queryVec, vec);
                if (keywordIds.contains(a.getId())) score = Math.max(score, 0.4) + 0.2;
                if (score > 0.03) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", a.getId());
                    m.put("title", a.getTitle());
                    m.put("summary", a.getSummary());
                    m.put("score", Math.round(score * 1000.0) / 1000.0);
                    m.put("snippet", TokenizerUtil.extractSnippet(
                            contentCache.getOrDefault(a.getId(), ""), query, 150));
                    scored.add(m);
                }
            }
            scored.sort((x, y) -> Double.compare((double) y.get("score"), (double) x.get("score")));
            return scored.subList(0, Math.min(topK, scored.size()));
        } finally {
            lock.readLock().unlock();
        }
    }

    // ── 相关文章 ──
    public List<Map<String, Object>> relatedArticles(Long articleId, int n) {
        lock.readLock().lock();
        try {
            if (tfidfCache.isEmpty()) {
                lock.readLock().unlock();
                buildIndex();
                lock.readLock().lock();
            }
            double[] targetVec = tfidfCache.get(articleId);
            if (targetVec == null) return List.of();

            List<Article> others = articleMapper.selectList(
                    new LambdaQueryWrapper<Article>()
                            .eq(Article::getStatus, 1)
                            .ne(Article::getId, articleId));

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
        } finally {
            lock.readLock().unlock();
        }
    }

    // ── 搜索建议 ──
    public List<String> suggest(String input) {
        if (input == null || input.isBlank()) return List.of();
        // 本地朴素推荐：匹配标题关键词
        return articleMapper.selectList(
                        new LambdaQueryWrapper<Article>()
                                .eq(Article::getStatus, 1)
                                .like(Article::getTitle, input)
                                .last("LIMIT 5"))
                .stream()
                .map(Article::getTitle)
                .collect(Collectors.toList());
    }

    public void refreshIndex() {
        vocabulary = List.of();
        tfidfCache = Map.of();
        contentCache = Map.of();
        buildIndex();
    }
}
