package befly.user.controller;

import befly.common.annotations.LoginUser;
import befly.user.dto.LoginResponse;
import befly.user.dto.SocialIdRequest;
import befly.user.service.GateWayService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import befly.common.apiPayload.ApiResponse;
import befly.common.code.status.SuccessStatus;
import befly.user.domain.User;
import befly.user.dto.*;
import befly.user.service.EmailDuplication;
import befly.user.service.SignInService;
import befly.user.service.SignUpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@RestController
@Slf4j
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SignUpService signUpService;
    private final SignInService signInService;
    private final EmailDuplication emailDuplication;
    private final GateWayService gateWayService;


    /**
     * 회원가입 로직
     * @param signUpRequest 실명, 닉네임, 이메일, 패스워드 받음
     * TODO 추후 profile img를 url로 저장할 수 있도록 로직 수정 필요
     * @return 아직 없음
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<String>> signUp(@RequestBody SignUpRequest signUpRequest) {
        log.info("signUp Request : {}", signUpRequest);
        User signUpUser = signUpService.signUp(signUpRequest);
        log.info("signUp Success : {}", signUpUser);

        ApiResponse<String> response = ApiResponse.onSuccess("회원가입이 완료되었습니다.");
        return ResponseEntity.ok(response);
    }


    @PostMapping("/signin")
    public ApiResponse<TokenResponse> signIn(@RequestBody SignInRequest signInRequest) {
        log.info("signIn Request : {}", signInRequest);
        ApiResponse<TokenResponse> apiResponse = signInService.signIn(signInRequest);
        log.info("signIn Success");
        return apiResponse;
    }

    @GetMapping("/test")
    public String test(@LoginUser Long userId) {
        return userId.toString();
    }


    /**
     * 이메일 중복 체크
     * @param Email 가입 ID 겸 이메일
     * @return 함수 반환값은 항상 True. 만약 중복 발생 시 서비스에서 예외 던짐
     */
    @GetMapping("/email/duplication")
    public String checkNicknameDuplication(@RequestParam String Email) {
        log.info("Email duplication check: {}", Email);
        if(!emailDuplication.isDuplication(Email)) {
            log.info("Email duplication check success: {}, No email Duplication", Email);
        }
        return SuccessStatus._OK.getMessage();
    }



    @PostMapping("/oauth2")
    public LoginResponse oauth2(@RequestBody SocialIdRequest socialIdRequest) {
        log.info("SocialId Request : {}", socialIdRequest.getOauth2Id());
        return gateWayService.findUserBySocialId(socialIdRequest.getOauth2Id());
    }

    @GetMapping("/refresh")
    public LoginResponse refreshToken(@LoginUser Long userId, HttpServletResponse response) {
        log.info("Refresh Token Request : {}", userId);
        LoginResponse loginResponse = gateWayService.generateLoginResponse(userId);
        //https 배포시 Secure 추가
        response.addHeader("Set-Cookie", String.format(
                "accessToken=%s; Path=/; HttpOnly; SameSite=None",
                loginResponse.getAccessToken()
        ));
        response.addHeader("Set-Cookie", String.format(
                "refreshToken=%s; Path=/; HttpOnly; SameSite=None",
                loginResponse.getRefreshToken()
        ));
        return loginResponse;
    }

    @GetMapping("/cd-test")
    public ApiResponse<String> cdTest() {
        String payload = "CD test successful at " + Instant.now().toString();
        log.info(payload);
        return ApiResponse.onSuccess(payload);
    }
}
