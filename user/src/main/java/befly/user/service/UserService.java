package befly.user.service;

import befly.common.exception.RestApiException;
import befly.common.service.S3Service;
import befly.user.domain.User;
import befly.user.dto.ImageUploadResponse;
import befly.user.dto.UserProfileResponse;
import befly.user.dto.UserListResponse;
import befly.user.repository.userRepository.UserRepository;
import befly.user.status.UserErrorStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
        user.updateNickName(newNickname);
        return user;
    }

    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RestApiException(UserErrorStatus.MEMBER_NOT_FOUND));
        return UserProfileResponse.builder()
                .nickName(user.getNickName())
                .clientId(user.getClientId())
                .profileImg(user.getProfileImg())
                .wing(user.getWing())
                .badge(user.getBadge())
                .build();
    }

    public ImageUploadResponse getImageUploadUrl(String key) {
        String preSignedUrl = s3Service.createPreSignedUrl(key, "PUT");
        String imageUrl = s3Service.getImageUrl(key);
        
        return ImageUploadResponse.builder()
                .preSignedUrl(preSignedUrl)
                .imageUrl(imageUrl)
                .build();
    }

    @Transactional
    public User updateProfileImage(Long userId, String imageUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RestApiException(UserErrorStatus.MEMBER_NOT_FOUND));
        
        user.updateProfileImg(imageUrl);
        return user;
    }

    @Transactional
    public void addWing(Long userId, Long wing) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RestApiException(UserErrorStatus.MEMBER_NOT_FOUND));
        
        user.addWing(wing);
        updateBadgeByWing(user);
    }

    private void updateBadgeByWing(User user) {
        Long wing = user.getWing();
        Long badge = 0L;
        
        if (wing >= 1250 ) {
            badge = 8L;
        } else if (wing >= 850) {
            badge = 7L;
        } else if (wing >= 550) {
            badge = 5L;
        }
        else if (wing >= 330) {
            badge = 4L;
        }
        else if (wing >= 180) {
            badge = 3L;
        }
        else if (wing >= 90) {
            badge = 2L;
        }
        else if (wing >= 30) {
            badge = 1L;
        }
        else {
            badge = 0L;
        }
        
        user.updateBadge(badge);
    }

    @Transactional
    public UserListResponse getUsersByIds(List<Long> userIds) {
        List<User> users = userRepository.findAllById(userIds);
        
        // 요청된 userId 중 일부가 존재하지 않는 경우
        if (users.size() != userIds.size()) {
            throw new RestApiException(UserErrorStatus.MEMBER_NOT_FOUND);
        }

        List<UserProfileResponse> userProfiles = users.stream()
                .map(user -> UserProfileResponse.builder()
                        .nickName(user.getNickName())
                        .profileImg(user.getProfileImg())
                        .wing(user.getWing())
                        .badge(user.getBadge())
                        .loginType(user.getLoginType().name())
                        .build())
                .toList();

        return UserListResponse.builder()
                .users(userProfiles)
                .build();
    }

    public String getNicknameById(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getNickName())
                .orElseThrow(() -> new RestApiException(UserErrorStatus.MEMBER_NOT_FOUND));
    }


}