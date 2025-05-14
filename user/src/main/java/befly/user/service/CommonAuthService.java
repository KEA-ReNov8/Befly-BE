package befly.user.service;

import befly.common.apiPayload.ApiResponse;
import befly.common.code.status.GlobalErrorStatus;
import befly.common.exception.RestApiException;
import befly.user.config.JwtProvider;
import befly.user.domain.User;
import befly.user.dto.commonAuth.SignInRequest;
import befly.user.dto.commonAuth.SignUpRequest;
import befly.user.dto.commonAuth.TokenResponse;
import befly.user.dto.gatewayAuth.response.GatewayLoginResponse;
import befly.user.dto.gatewayAuth.response.GatewayValidateResponse;
import befly.user.repository.userRepository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommonAuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository; // Repository 추가
    private final JwtProvider jwtProvider;

    /**
     * 회원가입 로직
     * @param signUpRequest 유저 정보 담겨있음
     * @return user: 저장된 유저 객체
     */
    @Transactional
    public User signUp(SignUpRequest signUpRequest) {
        log.info("SignUp request started for userName: {}", signUpRequest.getUserName());
//       check email, nickname duplicate
        checkForDuplicates(signUpRequest);
//        PasswordEncoding
        String encodedPassword = encodePassword(signUpRequest.getPassword());
//        save user
        User user = saveUser(signUpRequest, encodedPassword);

        log.info("SignUp completed for userName: {} with userId: {}", user.getUserName(), user.getUserId());
        return user;

    }
    private void checkForDuplicates(SignUpRequest signUpRequest) {
        if (userRepository.existsByNickname(signUpRequest.getNickname())) {
            throw new RestApiException(GlobalErrorStatus.DUPLICATE_NICKNAME);
        }
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RestApiException(GlobalErrorStatus.DUPLICATE_EMAIL);
        }
    }
    //HACK URL 이미지 경로 하드코딩되어있음
    private User saveUser(SignUpRequest signUpRequest, String encodedPassword) {
        return userRepository.save(User.builder()
                .userName(signUpRequest.getUserName())
                .nickname(signUpRequest.getNickname())
                .email(signUpRequest.getEmail())
                .password(encodedPassword)
                .wing(0L)
                .badge(0L)
                .profileImg("test.url")
                .build());
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }


    @Transactional
    public TokenResponse signIn(SignInRequest signInRequest) {
        log.info("SignIn request started for email: {}", signInRequest.getEmail());

        // 1. User 정보 조회
        User user = userRepository.findByEmail(signInRequest.getEmail())
                .orElseThrow(() -> new RestApiException(GlobalErrorStatus.MEMBER_NOT_FOUND));

        // 2. 비밀번호 확인
        if (!passwordEncoder.matches(signInRequest.getPassword(), user.getPassword())) {
            throw new RestApiException(GlobalErrorStatus.PWD_INVALID);
        }
        log.info("SignIn completed for email: {}", signInRequest.getEmail());

        // 3. JWT 토큰 생성 및 반환
        String accessToken = jwtProvider.generateAccessToken(user.getUserId().toString());
        String refreshToken = jwtProvider.generateRefreshToken(user.getUserId().toString());
        return makeTokenObject(accessToken, refreshToken);
    }

    private static TokenResponse makeTokenObject(String accessToken, String refreshToken) {
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken(accessToken);
        tokenResponse.setRefreshToken(refreshToken);
        return tokenResponse;
    }
}