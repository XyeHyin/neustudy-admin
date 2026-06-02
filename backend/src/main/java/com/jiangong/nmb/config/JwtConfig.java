package com.jiangong.nmb.config;

import com.jiangong.nmb.utils.JWTUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Value("${app.jwt.secret:change-me-jwt-secret}")
    private String secret;

    @PostConstruct
    public void init() {
        JWTUtil.setSecret(secret);
    }
}
