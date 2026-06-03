package com.xyehyin.hexuanning.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Jwt jwt = new Jwt();
    private Admin admin = new Admin();

    @Data
    public static class Jwt {
        private String secret = "change-me-jwt-secret";
    }

    @Data
    public static class Admin {
        private String username = "admin";
        private String password = "change-me";
        private String nickname = "Super Admin";
        private String email = "admin@example.com";
        private String phone = "10000000000";
    }
}
