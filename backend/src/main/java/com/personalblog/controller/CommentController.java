package com.personalblog.controller;

import com.personalblog.dto.Result;
import com.personalblog.entity.Comment;
import com.personalblog.service.CommentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/comment")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public Result<Void> submit(@RequestBody Comment comment) {
        commentService.submit(comment);
        return Result.success();
    }

    @GetMapping("/{articleId}")
    public Result<java.util.List<Comment>> list(@PathVariable Long articleId) {
        return Result.success(commentService.approvedComments(articleId));
    }
}
