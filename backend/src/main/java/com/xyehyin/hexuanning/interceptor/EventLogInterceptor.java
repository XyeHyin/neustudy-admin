package com.xyehyin.hexuanning.interceptor;

import com.xyehyin.hexuanning.service.EventLogService;
import com.xyehyin.hexuanning.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 拦截器：在接口成功返回后记录用户操作事件日志
 */
@Component
@RequiredArgsConstructor
public class EventLogInterceptor implements HandlerInterceptor {

    private final EventLogService eventLogService;
    private final UserService userService;

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) throws Exception {
        // 仅在 HTTP 200 并且用户已认证的情况下记录日志
        if (response.getStatus() == HttpServletResponse.SC_OK && "POST".equalsIgnoreCase(request.getMethod())) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof Long) {
                Long userId = (Long) auth.getPrincipal();
                // 获取用户名
                String username;
                var user = userService.findById(userId);
                if (user != null) {
                    username = user.getUsername();
                } else {
                    username = userId.toString();
                }
                String uri = request.getRequestURI();
                String method = request.getMethod();
                // 尝试获取 @Operation 注解的 summary
                String description = "";
                if (handler instanceof HandlerMethod) {
                    Operation op = ((HandlerMethod) handler).getMethodAnnotation(Operation.class);
                    if (op != null) {
                        description = op.summary();
                    }
                }
                // 记录事件日志
                eventLogService.logEvent(userId, username, method, uri, description);
            }
        }
    }
} 