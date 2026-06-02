package com.jiangong.nmb.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.openai")
public class AiProperties {
    private String baseUrl = "https://api.openai.com/v1";
    private String token = "change-me";
    private String model = "gpt-4o-mini";
}
