package befly.user.controller;

import befly.common.code.status.SuccessStatus;
import befly.user.service.UserService;
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

}
