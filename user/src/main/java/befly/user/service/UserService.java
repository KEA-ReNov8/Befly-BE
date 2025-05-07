package befly.user.service;

import befly.common.code.status.GlobalErrorStatus;
import befly.common.exception.RestApiException;
import befly.user.config.JwtProvider;
import befly.user.dto.gatewayAuth.response.GatewayLoginResponse;
import befly.user.dto.gatewayAuth.response.GatewayValidateResponse;
import befly.user.repository.userRepository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    @Transactional
    public boolean isNickNameDuplication(String email) {
        if(userRepository.existsByEmail(email)) {
            throw new RestApiException(GlobalErrorStatus.DUPLICATE_EMAIL);
        }
        return false;
    }
}