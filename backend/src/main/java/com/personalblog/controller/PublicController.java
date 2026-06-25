package com.personalblog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personalblog.dto.Result;
import com.personalblog.entity.*;
import com.personalblog.mapper.SiteConfigMapper;
import com.personalblog.service.PublicService;
import com.personalblog.vo.ArticleVO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Validated
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
    public Result<Page<ArticleVO>> articlePage(
            @RequestParam(defaultValue = "1") @Min(1) int pageNum,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String keyword) {
        Page<Article> page = publicService.articlePage(pageNum, pageSize, categoryId, tagId, keyword);
        Page<ArticleVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(ArticleVO::from).collect(Collectors.toList()));
        return Result.success(voPage);
    }

    @GetMapping("/article/{id}")
    public Result<Map<String, Object>> articleDetail(@PathVariable Long id) {
        Article article = publicService.getArticle(id);
        List<Tag> tags = publicService.getArticleTags(id);
        Article prev = publicService.getPrevArticle(id);
        Article next = publicService.getNextArticle(id);
        return Result.success(Map.of(
                "article", ArticleVO.from(article),
                "tags", tags,
                "prev", ArticleVO.from(prev),
                "next", ArticleVO.from(next)
        ));
    }

    @GetMapping("/article/latest")
    public Result<List<ArticleVO>> latest(@RequestParam(defaultValue = "5") int n) {
        return Result.success(publicService.latestArticles(n).stream()
                .map(ArticleVO::from).toList());
    }

    @GetMapping("/article/hot")
    public Result<List<ArticleVO>> hot(@RequestParam(defaultValue = "5") int n) {
        return Result.success(publicService.hotArticles(n).stream()
                .map(ArticleVO::from).toList());
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
