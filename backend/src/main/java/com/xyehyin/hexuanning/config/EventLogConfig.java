package com.xyehyin.hexuanning.config;

import com.xyehyin.hexuanning.interceptor.EventLogInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 配置拦截器以记录事件日志
 */
@Configuration
@RequiredArgsConstructor
public class EventLogConfig implements WebMvcConfigurer {

    private final EventLogInterceptor eventLogInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(eventLogInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/**",      // 登录注册无需记录
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/webjars/**",
                        "/notifications/**",
                        "/uploads/**",
                        "/favicon.ico",
                        "/error"
                );
    }
} 
