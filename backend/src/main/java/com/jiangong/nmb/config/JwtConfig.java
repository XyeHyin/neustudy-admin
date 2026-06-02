package com.jiangong.nmb.config;

import com.jiangong.nmb.config.properties.AppProperties;
import com.jiangong.nmb.utils.JWTUtil;
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
