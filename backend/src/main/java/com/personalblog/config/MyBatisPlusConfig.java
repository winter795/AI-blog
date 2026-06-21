package com.personalblog.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.personalblog.mapper")
public class MyBatisPlusConfig {
}
