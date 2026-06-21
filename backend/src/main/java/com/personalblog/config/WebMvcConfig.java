package com.personalblog.config;

import com.personalblog.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;
    private final String uploadPath;

    public WebMvcConfig(LoginInterceptor loginInterceptor,
                        @Value("${blog.upload-path:uploads}") String uploadPath) {
        this.loginInterceptor = loginInterceptor;
        this.uploadPath = uploadPath;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/user/login", "/api/public/**", "/api/site/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + Paths.get(uploadPath).toAbsolutePath() + "/");
    }
}
