package befly.user.service;

import befly.common.code.status.GlobalErrorStatus;
import befly.common.exception.RestApiException;
import befly.user.domain.User;
import befly.user.dto.SignUpRequest;
import befly.user.repository.userRepository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class SignUpService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입 로직
     * @param signUpRequest 유저 정보 담겨있음
     * @return user: 저장된 유저 객체
     */
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
        if (userRepository.existsByNickName(signUpRequest.getNickName())) {
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
                .nickName(signUpRequest.getNickName())
                .email(signUpRequest.getEmail())
                .password(encodedPassword)
                .wing(0L)
                .badge(0L)
                .profileImg("test.url")
                .build());
    }

    String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
}
