package com.personalblog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personalblog.dto.Result;
import com.personalblog.entity.Comment;
import com.personalblog.service.CommentService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/admin/comment")
public class AdminCommentController {

    private final CommentService commentService;

    public AdminCommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/page")
    public Result<Page<Comment>> page(
            @RequestParam(defaultValue = "1") @Min(1) int pageNum,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int pageSize,
            @RequestParam(required = false) Integer status) {
        return Result.success(commentService.page(pageNum, pageSize, status));
    }

    @PutMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable Long id,
                              @RequestParam @Min(0) @Max(2) Integer status) {
        commentService.audit(id, status);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        commentService.delete(id);
        return Result.success();
    }
}
