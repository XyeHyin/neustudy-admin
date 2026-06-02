package com.jiangong.nmb.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * AI配置类
 */
@Configuration
public class AIConfig {

    @Value("${ai.openai.baseUrl:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${ai.openai.token:change-me}")
    private String token;

    @Value("${ai.openai.model:gpt-4o-mini}")
    private String model;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(token)
                .modelName(model)
                .temperature(1.0)
                .timeout(Duration.ofSeconds(60))
                .maxRetries(3)
                .build();
    }
}
