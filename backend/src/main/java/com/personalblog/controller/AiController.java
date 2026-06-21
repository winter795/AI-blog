package com.personalblog.controller;

import com.personalblog.config.AiConfig;
import com.personalblog.dto.Result;
import com.personalblog.service.AiService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AiController {

    private final AiService aiService;
    private final AiConfig aiConfig;

    public AiController(AiService aiService, AiConfig aiConfig) {
        this.aiService = aiService;
        this.aiConfig = aiConfig;
    }

    @GetMapping("/public/ai/search")
    public SseEmitter search(@RequestParam String q) {
        if (!aiConfig.isEnabled()) {
            SseEmitter e = new SseEmitter();
            e.completeWithError(new RuntimeException("AI功能未启用"));
            return e;
        }
        try {
            List<Map<String, Object>> sources = aiService.hybridSearch(q, 5);
            return aiService.chatStream(q, sources);
        } catch (Exception ex) {
            SseEmitter e = new SseEmitter();
            e.completeWithError(ex);
            return e;
        }
    }

    @GetMapping("/public/ai/suggest")
    public Result<List<String>> suggest(@RequestParam String q) {
        if (!aiConfig.isEnabled()) return Result.success(List.of());
        try {
            return Result.success(aiService.suggest(q));
        } catch (Exception e) {
            return Result.success(List.of());
        }
    }

    @GetMapping("/public/ai/related/{articleId}")
    public Result<List<Map<String, Object>>> related(@PathVariable Long articleId) {
        if (!aiConfig.isEnabled()) return Result.success(List.of());
        try {
            return Result.success(aiService.relatedArticles(articleId, 5));
        } catch (Exception e) {
            return Result.success(List.of());
        }
    }

    @PostMapping("/admin/ai/summary")
    public Result<String> summary(@RequestBody Map<String, String> body) {
        if (!aiConfig.isEnabled()) return Result.error("AI功能未启用");
        try {
            return Result.success(aiService.autoSummary(body.get("content")));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/admin/ai/tags")
    public Result<List<String>> tags(@RequestBody Map<String, String> body) {
        if (!aiConfig.isEnabled()) return Result.success(List.of());
        try {
            return Result.success(aiService.autoTags(body.get("content")));
        } catch (Exception e) {
            return Result.success(List.of());
        }
    }
}
