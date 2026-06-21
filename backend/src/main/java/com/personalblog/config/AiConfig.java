package com.personalblog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiConfig {
    private String apiKey = "";
    private String baseUrl = "https://api.openai.com/v1";
    private String model = "gpt-4o-mini";
    private String embeddingModel = "text-embedding-3-small";
    private boolean enabled = false;
}
