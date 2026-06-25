package com.personalblog.controller;

import com.personalblog.config.AiConfig;
import com.personalblog.dto.Result;
import com.personalblog.service.AiAssistantService;
import com.personalblog.service.AiChatService;
import com.personalblog.service.AiSearchService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AiController {

    private final AiChatService chatService;
    private final AiSearchService searchService;
    private final AiAssistantService assistantService;
    private final AiConfig aiConfig;

    public AiController(AiChatService chatService, AiSearchService searchService,
                        AiAssistantService assistantService, AiConfig aiConfig) {
        this.chatService = chatService;
        this.searchService = searchService;
        this.assistantService = assistantService;
        this.aiConfig = aiConfig;
    }

    /** SSE 流式 AI 搜索问答 */
    @GetMapping("/public/ai/search")
    public SseEmitter search(@RequestParam String q) {
        if (!aiConfig.isEnabled()) {
            SseEmitter e = new SseEmitter();
            e.completeWithError(new RuntimeException("AI功能未启用"));
            return e;
        }
        try {
            return chatService.chatStream(q);
        } catch (Exception ex) {
            SseEmitter e = new SseEmitter();
            e.completeWithError(ex);
            return e;
        }
    }

    /** 搜索建议：优先 AI 生成，失败降级本地匹配 */
    @GetMapping("/public/ai/suggest")
    public Result<List<String>> suggest(@RequestParam String q) {
        if (!aiConfig.isEnabled()) return Result.success(List.of());
        try {
            List<String> suggestions = assistantService.suggest(q);
            if (suggestions.isEmpty()) suggestions = searchService.suggest(q);
            return Result.success(suggestions);
        } catch (Exception e) {
            return Result.success(searchService.suggest(q));
        }
    }

    /** 相关文章推荐 */
    @GetMapping("/public/ai/related/{articleId}")
    public Result<List<Map<String, Object>>> related(@PathVariable Long articleId) {
        if (!aiConfig.isEnabled()) return Result.success(List.of());
        try {
            return Result.success(searchService.relatedArticles(articleId, 5));
        } catch (Exception e) {
            return Result.success(List.of());
        }
    }

    /** AI 生成摘要 */
    @PostMapping("/admin/ai/summary")
    public Result<String> summary(@RequestBody Map<String, String> body) {
        if (!aiConfig.isEnabled()) return Result.error("AI功能未启用");
        try {
            return Result.success(assistantService.autoSummary(body.get("content")));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /** AI 推荐标签 */
    @PostMapping("/admin/ai/tags")
    public Result<List<String>> tags(@RequestBody Map<String, String> body) {
        if (!aiConfig.isEnabled()) return Result.success(List.of());
        try {
            return Result.success(assistantService.autoTags(body.get("content")));
        } catch (Exception e) {
            return Result.success(List.of());
        }
    }
}
