package com.personalblog.interceptor;

import com.personalblog.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public LoginInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录\"}");
            return false;
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Token无效或已过期\"}");
            return false;
        }

        String role = jwtUtil.getRole(token);
        request.setAttribute("userId", jwtUtil.getUserId(token));
        request.setAttribute("role", role != null ? role : "guest");

        // 管理 API 仅限 admin 角色
        String path = request.getRequestURI();
        if (path.startsWith("/api/admin/") || path.startsWith("/api/article")
                || path.startsWith("/api/category") || path.startsWith("/api/tag")) {
            if (!"admin".equals(role)) {
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":403,\"message\":\"权限不足，仅管理员可操作\"}");
                return false;
            }
        }
        return true;
    }
}
