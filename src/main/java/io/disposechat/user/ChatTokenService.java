package io.disposechat.user;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class ChatTokenService {

    @Value("${jwt.key}")
    private String secret;

    @Value("${jwt.validity}")
    private long validity;

    public String encodeToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
//                .setExpiration(new Date(System.currentTimeMillis() + validity * 1000))
                .signWith(Keys.hmacShaKeyFor( secret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    public String parseUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token).getBody().getSubject();
    }

}


