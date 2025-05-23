package befly.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final StringRedisTemplate redisTemplate;

    // RefreshToken 저장
    public void saveRefreshToken(Long userId, String refreshToken, long expireMillis) {
        redisTemplate.opsForValue().set("refresh:" + userId, refreshToken, expireMillis, TimeUnit.MILLISECONDS);
    }

    // RefreshToken 검증
    public boolean isValidRefreshToken(Long userId, String refreshToken) {
        String savedToken = redisTemplate.opsForValue().get("refresh:" + userId);
        return refreshToken.equals(savedToken);
    }

    // 로그아웃 (RefreshToken 삭제)
    public void logout(Long userId) {
        redisTemplate.delete("refresh:" + userId);
    }
}