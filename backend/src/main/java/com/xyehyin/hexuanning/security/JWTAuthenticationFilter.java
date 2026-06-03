package com.xyehyin.hexuanning.security;

import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.xyehyin.hexuanning.common.ApiResponse;
import com.xyehyin.hexuanning.entity.Permission;
import com.xyehyin.hexuanning.entity.User;
import com.xyehyin.hexuanning.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final UserService userService;

    public JWTAuthenticationFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (!JWTUtil.verifyToken(token)) {
                SecurityContextHolder.clearContext();
                writeJson(response, HttpStatus.HTTP_UNAUTHORIZED, ApiResponse.error(HttpStatus.HTTP_UNAUTHORIZED, "登录已失效，请重新登录"));
                return;
            }
            Long userId = JWTUtil.getUserId(token);
            User user = userId == null ? null : userService.findById(userId);
            if (user == null) {
                writeJson(response, HttpStatus.HTTP_UNAUTHORIZED, ApiResponse.error(HttpStatus.HTTP_UNAUTHORIZED, "用户不存在"));
                return;
            }
            if (!user.getEnabled()) {
                writeJson(response, HttpStatus.HTTP_FORBIDDEN, ApiResponse.error(HttpStatus.HTTP_FORBIDDEN, "用户被禁用"));
                return;
            }
            Set<String> permissionCodes = user.getRole() == null || user.getRole().getPermissions() == null
                    ? Set.of()
                    : user.getRole().getPermissions().stream().map(Permission::getCode).collect(Collectors.toSet());
            List<SimpleGrantedAuthority> authorities = permissionCodes.stream().map(SimpleGrantedAuthority::new).toList();
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    private void writeJson(HttpServletResponse response, int status, ApiResponse<?> body) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSONUtil.toJsonStr(body));
    }
}
