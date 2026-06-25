package com.personalblog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personalblog.dto.Result;
import com.personalblog.dto.ArticleSaveRequest;
import com.personalblog.entity.Article;
import com.personalblog.service.ArticleService;
import com.personalblog.vo.ArticleVO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping("/api/article")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("/page")
    public Result<Page<ArticleVO>> page(
            @RequestParam(defaultValue = "1") @Min(1) int pageNum,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long categoryId) {
        Page<Article> page = articleService.page(pageNum, pageSize, keyword, status, categoryId);
        Page<ArticleVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(ArticleVO::from).collect(Collectors.toList()));
        return Result.success(voPage);
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        Article article = articleService.getById(id);
        List<Long> tagIds = articleService.getTagIds(id);
        return Result.success(Map.of("article", ArticleVO.from(article), "tagIds", tagIds));
    }

    @PostMapping
    public Result<Void> save(@RequestBody ArticleSaveRequest req) {
        Article article = toArticle(req);
        articleService.save(article, req.getTagIds());
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody ArticleSaveRequest req) {
        Article article = toArticle(req);
        articleService.update(article, req.getTagIds());
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

    private Article toArticle(ArticleSaveRequest req) {
        Article a = new Article();
        a.setId(req.getId());
        a.setTitle(req.getTitle());
        a.setSummary(req.getSummary());
        a.setContent(req.getContent());
        a.setCategoryId(req.getCategoryId());
        a.setStatus(req.getStatus() != null ? req.getStatus() : 0);
        return a;
    }
}
