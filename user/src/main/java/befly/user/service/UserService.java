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

    public User updateNickname(Long userId, String newNickname) {
        // 닉네임 중복 체크
        if (userRepository.existsByNickname(newNickname)) {
            throw new RestApiException(GlobalErrorStatus.DUPLICATE_NICKNAME);
        }

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RestApiException(GlobalErrorStatus.MEMBER_NOT_FOUND));

        // 닉네임 업데이트
        user = User.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .password(user.getPassword())
                .profileImg(user.getProfileImg())
                .wing(user.getWing())
                .badge(user.getBadge())
                .nickname(newNickname)
                .build();

        return userRepository.save(user);
    }

    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RestApiException(GlobalErrorStatus.MEMBER_NOT_FOUND));
        return UserProfileResponse.builder()
                .userName(user.getUserName())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .profileImg(user.getProfileImg())
                .wing(user.getWing())
                .badge(user.getBadge())
                .build();
    }
} 