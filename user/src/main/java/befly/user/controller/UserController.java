package befly.user.controller;

import befly.common.annotations.LoginUser;
import befly.common.apiPayload.ApiResponse;
import befly.common.code.status.SuccessStatus;
import befly.user.domain.User;
import befly.user.dto.UpdateNickNameRequest;
import befly.user.dto.UserProfileResponse;
import befly.user.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    /**
     * 이메일 중복 체크
     * @param Email 가입 ID 겸 이메일
     * @return 함수 반환값은 항상 True. 만약 중복 발생 시 서비스에서 예외 던짐
     */
    @GetMapping("/email/duplication")
    public String checkNicknameDuplication(@RequestParam String Email) {
        log.info("Email duplication check: {}", Email);
        if(!userService.isNickNameDuplication(Email)) {
            log.info("Email duplication check success: {}, No email Duplication", Email);
        }
        return SuccessStatus._OK.getMessage();
    }

    @PutMapping("/nickname")
    public ApiResponse<User> updateNickname(
            @LoginUser @Parameter(hidden = true) Long userId,
            @RequestBody UpdateNickNameRequest request) {
        User updatedUser = userService.updateNickname(userId, request.getNickName());
        return ApiResponse.onSuccess(updatedUser);
    }

    @GetMapping("/profile")
    public ApiResponse<UserProfileResponse> getProfile(@LoginUser @Parameter(hidden = true) Long userId) {
        UserProfileResponse profile = userService.getProfile(userId);
        return ApiResponse.onSuccess(profile);
    }

}
