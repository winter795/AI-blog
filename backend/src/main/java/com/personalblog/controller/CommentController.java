package com.personalblog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personalblog.dto.Result;
import com.personalblog.entity.Comment;
import com.personalblog.service.CommentService;
import com.personalblog.util.CaptchaUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/public/comment")
public class CommentController {

    private final CommentService commentService;
    private final CaptchaUtil captchaUtil;

    public CommentController(CommentService commentService, CaptchaUtil captchaUtil) {
        this.commentService = commentService;
        this.captchaUtil = captchaUtil;
    }

    @GetMapping("/captcha")
    public Result<Map<String, String>> captcha() {
        return Result.success(captchaUtil.generate());
    }

    @PostMapping
    public Result<Void> submit(@RequestBody Comment comment,
                               @RequestParam String token,
                               @RequestParam int answer) {
        if (!captchaUtil.verify(token, answer)) {
            return Result.error("验证码错误");
        }
        commentService.submit(comment);
        return Result.success();
    }

    @GetMapping("/{articleId}")
    public Result<java.util.List<Comment>> list(@PathVariable Long articleId) {
        return Result.success(commentService.approvedComments(articleId));
    }
}
