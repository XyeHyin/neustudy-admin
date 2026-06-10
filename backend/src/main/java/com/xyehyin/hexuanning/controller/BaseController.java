package com.xyehyin.hexuanning.controller;

import com.xyehyin.hexuanning.security.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.Arrays;
import java.util.Collection;

@CrossOrigin
public class BaseController {

    protected Long getCurrentUserId(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return JWTUtil.getUserId(token);
        }
        return null;
    }

    protected boolean hasAuthority(String authority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities != null && authorities.stream()
                .anyMatch(grantedAuthority -> authority.equals(grantedAuthority.getAuthority()));
    }

    protected boolean hasAnyAuthority(String... authorities) {
        return authorities != null && Arrays.stream(authorities).anyMatch(this::hasAuthority);
    }
}
