package com.personalblog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.personalblog.dto.LoginRequest;
import com.personalblog.dto.LoginResponse;
import com.personalblog.entity.User;
import com.personalblog.mapper.UserMapper;
import com.personalblog.util.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public UserService(UserMapper userMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        String md5Password = DigestUtils.md5DigestAsHex(
                request.getPassword().getBytes(StandardCharsets.UTF_8));
        if (!user.getPassword().equals(md5Password)) {
            throw new RuntimeException("用户名或密码错误");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(),
                user.getRole() != null ? user.getRole() : "guest");
        String role = user.getRole() != null ? user.getRole() : "guest";
        return new LoginResponse(token, user.getNickname(), user.getAvatar(), role);
    }
}
