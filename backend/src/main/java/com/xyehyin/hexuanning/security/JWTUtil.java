package com.xyehyin.hexuanning.security;

import cn.hutool.jwt.JWT;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class JWTUtil {
    private static final String DEFAULT_SECRET = "change-me-jwt-secret";
    private static volatile String secret = DEFAULT_SECRET;
    private static final long EXPIRATION_TIME = 1000 * 24 * 60 * 60;
    private static final String USER_ID = "user_id";
    private static final String USERNAME = "username";
    private static final String EXP = "exp";


    public static String generateToken(Long userId, String username) {
        Map<String, Object> payload = new HashMap<>();
        payload.put(USER_ID, userId);
        payload.put(USERNAME, username);
        payload.put(EXP, (System.currentTimeMillis() + EXPIRATION_TIME) / 1000);
        return cn.hutool.jwt.JWTUtil.createToken(payload, getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public static boolean verifyToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            JWT jwt = cn.hutool.jwt.JWTUtil.parseToken(token);
            return jwt.setKey(getSecret().getBytes(StandardCharsets.UTF_8)).verify() && !isExpired(jwt);
        } catch (RuntimeException e) {
            return false;
        }
    }

    public static Long getUserId(String token) {
        try {
            JWT jwt = cn.hutool.jwt.JWTUtil.parseToken(token);
            Object userId = jwt.getPayload(USER_ID);
            return userId != null ? Long.parseLong(userId.toString()) : null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static boolean isExpired(JWT jwt) {
        Object exp = jwt.getPayload(EXP);
        if (exp == null) {
            return true;
        }
        try {
            long expiresAtSeconds = exp instanceof Number number ? number.longValue() : Long.parseLong(exp.toString());
            return expiresAtSeconds <= System.currentTimeMillis() / 1000;
        } catch (RuntimeException e) {
            return true;
        }
    }

    private static String getSecret() {
        return secret == null || secret.isBlank() ? DEFAULT_SECRET : secret;
    }

    public static void setSecret(String newSecret) {
        secret = newSecret;
    }
}
