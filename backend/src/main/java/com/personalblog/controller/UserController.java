package com.personalblog.controller;

import com.personalblog.dto.LoginRequest;
import com.personalblog.dto.LoginResponse;
import com.personalblog.dto.Result;
import com.personalblog.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }
}
