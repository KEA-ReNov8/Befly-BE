package befly.user.service;

import befly.common.code.status.GlobalErrorStatus;
import befly.common.exception.RestApiException;
import befly.user.domain.User;
import befly.user.repository.userRepository.UserRepository;
import befly.user.dto.UserProfileResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User updateNickName(Long userId, String newNickName) {
        // 닉네임 중복 체크
        if (userRepository.existsByNickName(newNickName)) {
            throw new RestApiException(GlobalErrorStatus.DUPLICATE_NICKNAME);
        }

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RestApiException(GlobalErrorStatus.MEMBER_NOT_FOUND));

        // 닉네임만 업데이트
        user.updateNickName(newNickName);
        return userRepository.save(user);
    }

    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RestApiException(GlobalErrorStatus.MEMBER_NOT_FOUND));
        return UserProfileResponse.builder()
                .userName(user.getUserName())
                .nickName(user.getNickName())
                .email(user.getEmail())
                .profileImg(user.getProfileImg())
                .wing(user.getWing())
                .badge(user.getBadge())
                .build();
    }
} 