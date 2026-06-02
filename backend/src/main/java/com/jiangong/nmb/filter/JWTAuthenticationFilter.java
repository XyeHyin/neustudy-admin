package com.jiangong.nmb.filter;

import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.jiangong.nmb.common.ApiResponse;
import com.jiangong.nmb.entity.Permission;
import com.jiangong.nmb.entity.User;
import com.jiangong.nmb.service.UserService;
import com.jiangong.nmb.utils.JWTUtil;
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

    private UserService userService;

    public JWTAuthenticationFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (JWTUtil.verifyToken(token)) {
                Long userId = JWTUtil.getUserId(token);
                User user = userService.findById(userId);
                if (user == null) {
                    response.setStatus(HttpStatus.HTTP_UNAUTHORIZED);
                    response.setCharacterEncoding("UTF-8");
                    response.setContentType("application/json");
                    response.getWriter().write(JSONUtil.toJsonStr(ApiResponse.error(HttpStatus.HTTP_UNAUTHORIZED, "用户不存在")));
                    return;
                }
                if (!user.getEnabled()) {
                    response.setStatus(HttpStatus.HTTP_UNAUTHORIZED);
                    response.setCharacterEncoding("UTF-8");
                    response.setContentType("application/json");
                    response.getWriter().write(JSONUtil.toJsonStr(ApiResponse.error(HttpStatus.HTTP_FORBIDDEN, "用户被禁用")));
                    return;
                }
                Set<String> permissionCodes = user.getRole().getPermissions().stream().map(Permission::getCode).collect(Collectors.toSet());
                List<SimpleGrantedAuthority> authorities = permissionCodes.stream().map(SimpleGrantedAuthority::new).toList();
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }
}
