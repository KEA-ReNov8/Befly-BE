package befly.user.service;

import befly.common.exception.RestApiException;
import befly.common.service.S3Service;
import befly.user.domain.User;
import befly.user.dto.UserProfileResponse;
import befly.user.repository.userRepository.UserRepository;
import befly.user.status.UserErrorStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final S3Service s3Service;

    public boolean isNickNameDuplication(String ClientId) {
        if(userRepository.existsByClientId(ClientId)) {
            throw new RestApiException(UserErrorStatus.DUPLICATE_ClIENT_ID);
        }
        return false;
    }
    @Transactional
    public User updateNickname(Long userId, String newNickname) {
        // 닉네임 중복 체크
        if (userRepository.existsByNickName(newNickname)) {
            throw new RestApiException(UserErrorStatus.DUPLICATE_NICKNAME);
        }

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RestApiException(UserErrorStatus.MEMBER_NOT_FOUND));

        // 닉네임 업데이트
        user = User.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .clientId(user.getClientId())
                .password(user.getPassword())
                .profileImg(user.getProfileImg())
                .wing(user.getWing())
                .badge(user.getBadge())
                .nickName(newNickname)
                .build();

        return userRepository.save(user);
    }

    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RestApiException(UserErrorStatus.MEMBER_NOT_FOUND));
        return UserProfileResponse.builder()
                .userName(user.getUserName())
                .nickName(user.getNickName())
                .clientId(user.getClientId())
                .profileImg(user.getProfileImg())
                .wing(user.getWing())
                .badge(user.getBadge())
                .build();
    }

    @Transactional
    public User updateProfileImage(Long userId, String imageKey) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RestApiException(UserErrorStatus.MEMBER_NOT_FOUND));

        String imageUrl = s3Service.getImageUrl(imageKey);
        
        user = User.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .clientId(user.getClientId())
                .password(user.getPassword())
                .profileImg(imageUrl)
                .wing(user.getWing())
                .badge(user.getBadge())
                .nickName(user.getNickName())
                .build();

        return userRepository.save(user);
    }
}