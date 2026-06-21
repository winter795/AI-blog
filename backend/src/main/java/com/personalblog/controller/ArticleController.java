package com.personalblog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personalblog.dto.Result;
import com.personalblog.entity.Article;
import com.personalblog.service.ArticleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/article")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("/page")
    public Result<Page<Article>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long categoryId) {
        return Result.success(articleService.page(pageNum, pageSize, keyword, status, categoryId));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        Article article = articleService.getById(id);
        List<Long> tagIds = articleService.getTagIds(id);
        return Result.success(Map.of("article", article, "tagIds", tagIds));
    }

    @PostMapping
    public Result<Void> save(@RequestBody Article article,
                             @RequestParam(required = false) List<Long> tagIds) {
        articleService.save(article, tagIds);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Article article,
                               @RequestParam(required = false) List<Long> tagIds) {
        articleService.update(article, tagIds);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/toggle")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        articleService.toggleStatus(id);
        return Result.success();
    }
}
