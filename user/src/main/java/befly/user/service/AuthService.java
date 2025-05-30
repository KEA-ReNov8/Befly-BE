package befly.user.service;

import befly.common.exception.RestApiException;
import befly.user.config.JwtProvider;
import befly.user.status.UserErrorStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final StringRedisTemplate redisTemplate;
    private final JwtProvider jwtProvider;

    // RefreshToken 저장
    public void saveRefreshToken(Long userId, String refreshToken, long expireMillis) {
        redisTemplate.opsForValue().set("refresh:" + userId, refreshToken, expireMillis, TimeUnit.MILLISECONDS);
    }

    // RefreshToken 검증
    public Long validateRefreshToken(HttpServletRequest request) {
        String refreshToken = request.getHeader("X-Refresh-Token");
        if(refreshToken.isEmpty()) throw new RestApiException(UserErrorStatus.NOT_FOUND_REFRESH_TOKEN);

        Long userId = jwtProvider.getUserIdFromRefreshToken(refreshToken);
        String savedToken = redisTemplate.opsForValue().get("refresh:" + userId);

        if(!savedToken.equals(refreshToken)) throw new RestApiException(UserErrorStatus.NOT_FOUND_REFRESH_TOKEN);
        return userId;
    }

    // 로그아웃 (RefreshToken 삭제)
    public void logout(Long userId) {
        redisTemplate.delete("refresh:" + userId);
    }
}