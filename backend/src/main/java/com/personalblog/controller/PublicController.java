package com.personalblog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personalblog.dto.Result;
import com.personalblog.entity.Article;
import com.personalblog.entity.Category;
import com.personalblog.entity.FriendLink;
import com.personalblog.entity.Tag;
import com.personalblog.mapper.SiteConfigMapper;
import com.personalblog.service.PublicService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import com.personalblog.entity.SiteConfig;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final PublicService publicService;
    private final SiteConfigMapper configMapper;

    public PublicController(PublicService publicService, SiteConfigMapper configMapper) {
        this.publicService = publicService;
        this.configMapper = configMapper;
    }

    @GetMapping("/article/page")
    public Result<Page<Article>> articlePage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String keyword) {
        return Result.success(publicService.articlePage(pageNum, pageSize, categoryId, tagId, keyword));
    }

    @GetMapping("/article/{id}")
    public Result<Map<String, Object>> articleDetail(@PathVariable Long id) {
        Article article = publicService.getArticle(id);
        List<Tag> tags = publicService.getArticleTags(id);
        List<Article> prevNext = publicService.prevNext(id);
        return Result.success(Map.of(
                "article", article,
                "tags", tags,
                "prev", prevNext.get(0),
                "next", prevNext.get(1)
        ));
    }

    @GetMapping("/article/latest")
    public Result<List<Article>> latest(@RequestParam(defaultValue = "5") int n) {
        return Result.success(publicService.latestArticles(n));
    }

    @GetMapping("/article/hot")
    public Result<List<Article>> hot(@RequestParam(defaultValue = "5") int n) {
        return Result.success(publicService.hotArticles(n));
    }

    @GetMapping("/category")
    public Result<List<Category>> categories() {
        return Result.success(publicService.allCategories());
    }

    @GetMapping("/tag")
    public Result<List<Tag>> tags() {
        return Result.success(publicService.allTags());
    }

    @GetMapping("/friend-links")
    public Result<List<FriendLink>> friendLinks() {
        return Result.success(publicService.friendLinks());
    }

    @GetMapping("/config")
    public Result<Map<String, String>> config() {
        Map<String, String> map = new LinkedHashMap<>();
        configMapper.selectList(null).forEach(c -> map.put(c.getConfigKey(), c.getConfigValue()));
        return Result.success(map);
    }
}
