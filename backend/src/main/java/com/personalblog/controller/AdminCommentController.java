package com.personalblog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personalblog.dto.Result;
import com.personalblog.entity.Comment;
import com.personalblog.service.CommentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/comment")
public class AdminCommentController {

    private final CommentService commentService;

    public AdminCommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/page")
    public Result<Page<Comment>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status) {
        return Result.success(commentService.page(pageNum, pageSize, status));
    }

    @PutMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable Long id, @RequestParam Integer status) {
        commentService.audit(id, status);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        commentService.delete(id);
        return Result.success();
    }
}
