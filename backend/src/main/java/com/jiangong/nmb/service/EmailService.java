package com.jiangong.nmb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;
    private final MailProperties mailProperties;

    private static final int EXPIRE_MINUTES = 5;
    private static final String CODE_PREFIX = "email:code";

    @Async
    public void sendVerifyCode(String email) {
        String code = String.format("%06d", new Random().nextInt(999_999));
        // 存 Redis：key=email:code:{email}，过期 5 分钟
        redisTemplate.opsForValue().set(CODE_PREFIX + email, code, EXPIRE_MINUTES, TimeUnit.MINUTES);
        // 发送邮件
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(mailProperties.getUsername());
        msg.setTo(email);
        msg.setSubject("验证码");
        msg.setText("您的验证码为：" + code + "，有效期 " + EXPIRE_MINUTES + " 分钟。");
        mailSender.send(msg);

    }

    public boolean verifyCode(String email, String code) {
        String key = CODE_PREFIX + email;
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null && cached.equals(code)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }
}
