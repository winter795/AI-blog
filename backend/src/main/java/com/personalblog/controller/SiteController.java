package com.personalblog.controller;

import com.personalblog.dto.Result;
import com.personalblog.entity.Article;
import com.personalblog.entity.SiteStat;
import com.personalblog.service.CommentService;
import com.personalblog.service.PublicService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.personalblog.mapper.ArticleMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/site")
public class SiteController {

    private final PublicService publicService;
    private final CommentService commentService;
    private final ArticleMapper articleMapper;

    @Value("${blog.domain:http://localhost:5173}")
    private String siteDomain;

    public SiteController(PublicService publicService, CommentService commentService,
                          ArticleMapper articleMapper) {
        this.publicService = publicService;
        this.commentService = commentService;
        this.articleMapper = articleMapper;
    }

    @PostMapping("/pv")
    public Result<Void> pv(@RequestParam(required = false) String ip) {
        commentService.recordPV(ip != null ? ip : "unknown");
        return Result.success();
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        SiteStat today = commentService.todayStats();
        long totalPV = commentService.totalPV();
        long totalArticles = articleMapper.selectCount(
                new LambdaQueryWrapper<Article>().eq(Article::getStatus, 1));
        return Result.success(Map.of(
                "todayPV", today != null ? today.getPv() : 0,
                "todayUV", today != null ? today.getUv() : 0,
                "totalPV", totalPV,
                "totalArticles", totalArticles
        ));
    }

    @PutMapping("/article/{id}/like")
    public Result<Void> like(@PathVariable Long id) {
        Article article = articleMapper.selectById(id);
        if (article != null) {
            article.setLikeCount((article.getLikeCount() == null ? 0 : article.getLikeCount()) + 1);
            articleMapper.updateById(article);
        }
        return Result.success();
    }

    @GetMapping(value = "/sitemap.xml", produces = "application/xml;charset=UTF-8")
    public String sitemap() {
        List<Article> articles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>().eq(Article::getStatus, 1));
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        for (Article a : articles) {
            sb.append("  <url>\n");
            sb.append("    <loc>").append(siteDomain).append("/visitor/article/").append(a.getId()).append("</loc>\n");
            sb.append("    <lastmod>").append(a.getUpdatedAt().toLocalDate()).append("</lastmod>\n");
            sb.append("  </url>\n");
        }
        sb.append("</urlset>");
        return sb.toString();
    }

    @GetMapping(value = "/rss", produces = "application/xml;charset=UTF-8")
    public String rss() {
        List<Article> articles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>().eq(Article::getStatus, 1)
                        .orderByDesc(Article::getCreatedAt).last("LIMIT 20"));
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<rss version=\"2.0\">\n<channel>\n");
        sb.append("  <title>个人AI博客</title>\n");
        sb.append("  <link>").append(siteDomain).append("/visitor</link>\n");
        sb.append("  <description>个人AI博客 RSS 订阅</description>\n");
        for (Article a : articles) {
            sb.append("  <item>\n");
            sb.append("    <title>").append(escapeXml(a.getTitle())).append("</title>\n");
            sb.append("    <link>").append(siteDomain).append("/visitor/article/").append(a.getId()).append("</link>\n");
            sb.append("    <description>").append(escapeXml(a.getSummary() != null ? a.getSummary() : "")).append("</description>\n");
            sb.append("    <pubDate>").append(a.getCreatedAt()).append("</pubDate>\n");
            sb.append("  </item>\n");
        }
        sb.append("</channel>\n</rss>");
        return sb.toString();
    }

    private String escapeXml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }
}
