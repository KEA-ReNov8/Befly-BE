package befly.user.controller;

import befly.common.annotations.LoginUser;
import befly.user.dto.commonAuth.SignInRequest;
import befly.user.dto.commonAuth.SignUpRequest;
import befly.user.dto.commonAuth.TokenResponse;
import befly.user.dto.gatewayAuth.response.GatewayLoginResponse;
import befly.user.dto.gatewayAuth.request.GatewaySocialIdRequest;
import befly.user.dto.gatewayAuth.response.GatewayValidateResponse;
import befly.user.service.CommonAuthService;
import befly.user.service.GatewayAuthService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import befly.common.apiPayload.ApiResponse;
import befly.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import befly.user.service.AuthService;

@RestController
@Slf4j
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CommonAuthService commonAuthService;
    private final GatewayAuthService gateWayAuthService;
    private final AuthService authService;


    /**
     * 회원가입 로직
     * @param signUpRequest 실명, 닉네임, 이메일, 패스워드 받음
     * TODO 추후 profile img를 url로 저장할 수 있도록 로직 수정 필요
     * @return 아직 없음
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<String>> commonSignUpController(@RequestBody SignUpRequest signUpRequest) {
        log.info("signUp Request : {}", signUpRequest);
        User signUpUser = commonAuthService.signUp(signUpRequest);
        log.info("signUp Success : {}", signUpUser);

        ApiResponse<String> response = ApiResponse.onSuccess("회원가입이 완료되었습니다.");
        return ResponseEntity.ok(response);
    }
    @PostMapping("/signin")
    public ApiResponse<Void> commonSignInController(@RequestBody SignInRequest signInRequest,
                                                             HttpServletResponse response) {
        log.info("signIn Request : {}", signInRequest);
        TokenResponse tokenResponse = commonAuthService.signIn(signInRequest);
        response.addHeader("Authorization", tokenResponse.getAccessToken());
        response.addHeader("X-Refresh-Token", tokenResponse.getRefreshToken());
        log.info("signIn Success");
        return ApiResponse.onSuccess(null);
    }

    /**
     * 소셜 로그인시에 사용자가 존재하는지 확인하는 controller
     * @param gatewaySocialIdRequest
     * @return
     */
    @PostMapping("/oauth2")
    public GatewayLoginResponse oauth2AuthController(@RequestBody GatewaySocialIdRequest gatewaySocialIdRequest) {
        log.info("SocialId Request : {}", gatewaySocialIdRequest.getOauth2Id());
        return gateWayAuthService.findUserBySocialId(gatewaySocialIdRequest.getOauth2Id());
    }

    /**
     * 일반 요청시 요청하는 사용자가 존재하는 사용자 인지 확인하는 controller
     * @param userId
     * @return
     */
    @GetMapping("/exist/user")
    public GatewayValidateResponse validateUserController(@LoginUser @Parameter(hidden = true) Long userId) {
        log.info("SocialId Request : {}", userId);
        return gateWayAuthService.validateUserExist(userId);
    }

    @GetMapping("/refresh")
    public ApiResponse<Void> refreshTokenController(HttpServletRequest request,
                                                    HttpServletResponse response) {
        Long userId = authService.validateRefreshToken(request);
        GatewayLoginResponse gatewayLoginResponse = gateWayAuthService.generateLoginResponse(userId);
        //https 배포시 Secure 추가
        response.addHeader("Authorization", gatewayLoginResponse.getAccessToken());
        response.addHeader("X-Refresh-Token", gatewayLoginResponse.getRefreshToken());
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@LoginUser Long userId) {
        authService.logout(userId);
        return ApiResponse.onSuccess(null);
    }
}
