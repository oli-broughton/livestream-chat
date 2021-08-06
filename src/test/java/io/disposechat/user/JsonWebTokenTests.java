package io.disposechat.user;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

public class JsonWebTokenTests {

    @Test
    void parseSubject (){

        var key = "3ksLp2tanUM02v6HuVACQQaCl7NJzIvGPsxkrjsyVfDN7/PmLI2/uMzelDvtRjOxw3y8zTsWMtRrDh78YOyyL" +
                "HUmJcz5O2mn9bbfcBoWO0Wkg5UiW/dszW8wTUG1kbD+o2s/qeBDZO1PV+p093NbvLlgBHDtlDQOOgaEiAehyA2" +
                "5NWStMHzsbP9kLXT5uyVnMPVe7lB9anJHZtSS/J1fBc/zKVaQ53+BBNcRAw";

        long validity = 300;

        var subject = "username";

        String token = Jwts.builder()
                .setSubject(subject)
                .signWith(Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8)))
                .compact();

        var parsedSubject = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token).getBody().getSubject();

        Assertions.assertEquals(subject, parsedSubject);
    }
}
