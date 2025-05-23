package befly.user.config;


import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class JwtProvider {
    @Value("${jwt.access.secret}") String access;
    @Value("${jwt.access.expire}") Long accessExpiration;

    @Value("${jwt.refresh.secret}") String refresh;
    @Value("${jwt.refresh.expire}") Long refreshExpiration;

    public String generateAccessToken(String userId) {
        return getString(userId, access, accessExpiration);
    }

    public String generateRefreshToken(String userId) {
         return getString(userId, refresh, refreshExpiration);
    }

    public Long getUserIdFromRefreshToken(String refresh) {
        return Long.parseLong(Jwts.parserBuilder()
                .setSigningKey(refresh)
                .build()
                .parseClaimsJws(refresh)
                .getBody()
                .getSubject());
    }

    private String getString(String userId, String period, Long refreshExpiration) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .signWith(io.jsonwebtoken.SignatureAlgorithm.HS256, period)
                .setExpiration(java.util.Date.from(java.time.Instant.now().plusMillis(refreshExpiration))) //밀리세컨드 단위로 수정
                .compact();
    }

}
