package com.xyehyin.hexuanning.config;

import com.xyehyin.hexuanning.config.properties.AppProperties;
import com.xyehyin.hexuanning.security.JWTUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class JwtConfig {

    private final AppProperties appProperties;

    @PostConstruct
    public void init() {
        JWTUtil.setSecret(appProperties.getJwt().getSecret());
    }
}
