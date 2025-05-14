package befly.user.service;

import befly.user.config.JwtProvider;
import befly.user.dto.gatewayAuth.response.GatewayLoginResponse;
import befly.user.dto.gatewayAuth.response.GatewayValidateResponse;
import befly.user.repository.userRepository.UserRepository;
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

    public GatewayLoginResponse findUserBySocialId(String socialId) {
        return userRepository.findByEmail(socialId)
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