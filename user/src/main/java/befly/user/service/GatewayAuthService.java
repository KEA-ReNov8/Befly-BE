package befly.user.service;

import befly.common.exception.RestApiException;
import befly.user.config.JwtProvider;
import befly.user.dto.gatewayAuth.response.GatewayLoginResponse;
import befly.user.dto.gatewayAuth.response.GatewayValidateResponse;
import befly.user.repository.userRepository.UserRepository;
import befly.user.service.AuthService;
import befly.user.status.UserErrorStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GatewayAuthService {
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final AuthService authService;

    public GatewayLoginResponse findUserBySocialId(String socialId) {
        return userRepository.findByClientId(socialId)
                .map(user ->
                        createTokenResponse(user.getUserId())
                )
                .orElseGet(() -> buildLoginResponse(false, null, null));
    }

    public GatewayValidateResponse validateUserExist(Long userId) {
        return GatewayValidateResponse.builder()
                .existStatus(
                        userRepository.findById(userId)
                        .isPresent())
                .build();
    }


    @Transactional
    public GatewayLoginResponse generateLoginResponse(Long userId) {
        if (isUserExists(userId)) {
            return createTokenResponse(userId);
        } else {
            return buildLoginResponse(false, null, null);
        }
    }

    public boolean isUserExists(long userId) {
        return userRepository.findById(userId).isPresent();
    }

    private GatewayLoginResponse createTokenResponse(Long userId) {
        String id = userId.toString();
        String accessToken = jwtProvider.generateAccessToken(id);
        String refreshToken = jwtProvider.generateRefreshToken(id);
        long refreshTokenExpireMillis = 1000L * 60 * 60 * 24 * 7;
        authService.saveRefreshToken(userId, refreshToken, refreshTokenExpireMillis);
        return buildLoginResponse(true, accessToken, refreshToken);
    }

    private GatewayLoginResponse buildLoginResponse(boolean signUpStatus, String accessToken, String refreshToken) {
        return GatewayLoginResponse.builder()
                .signUpStatus(signUpStatus)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}