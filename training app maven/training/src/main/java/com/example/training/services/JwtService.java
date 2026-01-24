package com.example.training.services;

import com.example.training.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtService {
    public JwtService(@Value("${logging.api.key}") String catalog) {
        this.loginKey = catalog;
        this.key = Keys.hmacShaKeyFor(loginKey.getBytes());
    }

    private String loginKey;
    private SecretKey key;


    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .issuedAt(new Date())
                .expiration(Date.from(
                        Instant.now().plus(1, ChronoUnit.DAYS)
                ))
                .signWith(key)
                .compact();
    }

    public Long getUserId(String token) {
        return Long.valueOf(
                Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload()
                        .getSubject()
        );
    }
}