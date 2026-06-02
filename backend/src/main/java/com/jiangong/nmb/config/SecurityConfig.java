package com.jiangong.nmb.config;

import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.jiangong.nmb.common.ApiResponse;
import com.jiangong.nmb.filter.JWTAuthenticationFilter;
import com.jiangong.nmb.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, UserService userService) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable).sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).authorizeHttpRequests(auth -> auth
                // 登录、注册 放行
                .requestMatchers("/auth/login", "/auth/register", "/auth/code", "/auth/reset-password").permitAll().requestMatchers(HttpMethod.GET, "/v3/api-docs", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/index.html", "/swagger-ui/**", "/swagger-resources/**", "/webjars/**", "/favicon.ico").permitAll()
                // 其他所有接口都需要认证
                .anyRequest().authenticated()).exceptionHandling(eh -> eh.authenticationEntryPoint((request, response, authException) -> {
            response.setStatus(HttpStatus.HTTP_UNAUTHORIZED);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.getWriter().write(JSONUtil.toJsonStr(ApiResponse.error(HttpStatus.HTTP_UNAUTHORIZED, "未登录或登录已过期")));
        }).accessDeniedHandler((request, response, accessDeniedException) -> {
            response.setStatus(HttpStatus.HTTP_FORBIDDEN);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.getWriter().write(JSONUtil.toJsonStr(ApiResponse.error(HttpStatus.HTTP_FORBIDDEN, "权限不足")));
        })).addFilterBefore(new JWTAuthenticationFilter(userService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
