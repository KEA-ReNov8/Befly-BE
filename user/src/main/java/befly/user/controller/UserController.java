package befly.user.controller;

import befly.common.annotations.LoginUser;
import befly.common.apiPayload.ApiResponse;
import befly.common.code.status.SuccessStatus;
import befly.user.domain.User;
import befly.user.dto.UpdateNickNameRequest;
import befly.user.dto.UpdateProfileImageRequest;
import befly.user.dto.UserProfileResponse;
import befly.user.dto.ProfileImageResponse;
import befly.user.dto.UpdateNickNameResponse;
import befly.user.dto.ImageUploadResponse;
import befly.user.dto.UserListResponse;
import befly.user.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    /**
     * 이메일 중복 체크
     * @param ClientId 가입 ID 겸 이메일
     * @return 함수 반환값은 항상 True. 만약 중복 발생 시 서비스에서 예외 던짐
     */
    @GetMapping("/clientId/duplication")
    public String checkNicknameDuplication(@RequestParam String ClientId) {
        log.info("ClientId duplication check: {}", ClientId);
        if (!userService.isNickNameDuplication(ClientId)) {
            log.info("ClientId duplication check success: {}, No clientId Duplication", ClientId);
        }
        return SuccessStatus._OK.getMessage();
    }

    @PutMapping("/nickname")
    public ApiResponse<UpdateNickNameResponse> updateNickname(
            @LoginUser @Parameter(hidden = true) Long userId,
            @RequestBody UpdateNickNameRequest request) {
        User updatedUser = userService.updateNickname(userId, request.getNickName());
        UpdateNickNameResponse response = UpdateNickNameResponse.builder()
                .nickName(updatedUser.getNickName())
                .build();
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/profile")
    public ApiResponse<UserProfileResponse> getProfile(@LoginUser @Parameter(hidden = true) Long userId) {
        UserProfileResponse profile = userService.getProfile(userId);
        return ApiResponse.onSuccess(profile);
    }

    @PostMapping("/upload/image")
    public ApiResponse<ImageUploadResponse> getImageUploadUrl(
            @LoginUser @Parameter(hidden = true) Long userId,
            @RequestParam String imageKey) {
        ImageUploadResponse response = userService.getImageUploadUrl(imageKey);
        return ApiResponse.onSuccess(response);
    }

    @PutMapping("/profile/image")
    public ApiResponse<ProfileImageResponse> updateProfileImage(
            @LoginUser @Parameter(hidden = true) Long userId,
            @RequestBody UpdateProfileImageRequest request) {
        User updatedUser = userService.updateProfileImage(userId, request.getImageUrl());
        ProfileImageResponse response = ProfileImageResponse.builder()
                .userId(updatedUser.getUserId())
                .profileImg(updatedUser.getProfileImg())
                .build();
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/profiles")
    public ApiResponse<UserListResponse> getUsersByIds(@RequestParam List<Long> userIds) {
        return ApiResponse.onSuccess(userService.getUsersByIds(userIds));
    }

    @GetMapping("/getNickname/{userId}")
    public ApiResponse<String> getUserNicknameById(@PathVariable Long userId) {
        return ApiResponse.onSuccess(userService.getNicknameById(userId));
    }
}

//    http://user-service.backend.svc.cluster.local/user/getNickname/{userId}
