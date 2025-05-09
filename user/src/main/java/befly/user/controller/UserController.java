package befly.user.controller;

import befly.common.annotations.LoginUser;
import befly.common.apiPayload.ApiResponse;
import befly.common.code.status.SuccessStatus;
import befly.user.domain.User;
import befly.user.dto.UpdateNicknameRequest;
import befly.user.dto.UpdateNicknameResponse;
import befly.user.dto.UserProfileResponse;
import befly.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PutMapping("/nickname")
    public ApiResponse<UpdateNicknameResponse> updateNickname(
            @LoginUser Long userId,
            @RequestBody UpdateNicknameRequest request) {
        User updatedUser = userService.updateNickname(userId, request.getNickname());
        return ApiResponse.onSuccess(UpdateNicknameResponse.builder()
                .nickname(updatedUser.getNickname())
                .build());
    }

    @GetMapping("/profile")
    public ApiResponse<UserProfileResponse> getProfile(@LoginUser Long userId) {
        UserProfileResponse profile = userService.getProfile(userId);
        return ApiResponse.onSuccess(profile);
    }

    @PostMapping("/logout")
    public ApiResponse<SuccessStatus> logout() {
        return ApiResponse.onSuccess(SuccessStatus._OK);
    }
}