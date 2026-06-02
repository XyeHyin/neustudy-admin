package com.jiangong.nmb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.CrossOrigin;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableMethodSecurity
@CrossOrigin
@EnableAsync
@Slf4j
public class NeustudyAdminBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(NeustudyAdminBackendApplication.class, args);
        log.info("Neustudy Admin Backend 启动成功!");
    }
}
