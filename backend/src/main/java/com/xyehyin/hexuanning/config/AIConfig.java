package com.xyehyin.hexuanning.config;

import com.xyehyin.hexuanning.config.properties.AiProperties;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * AI配置类
 */
@Configuration
@RequiredArgsConstructor
public class AIConfig {

    private final AiProperties aiProperties;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .baseUrl(aiProperties.getBaseUrl())
                .apiKey(aiProperties.getToken())
                .modelName(aiProperties.getModel())
                .temperature(1.0)
                .timeout(Duration.ofSeconds(60))
                .maxRetries(3)
                .build();
    }
}
