package befly.community.controller;

import befly.common.annotations.LoginUser;
import befly.common.apiPayload.ApiResponse;
import befly.community.service.FreeEmpathyService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/community/free/{freeId}/empathy")
@RequiredArgsConstructor
public class    FreeEmpathyController {

    private final FreeEmpathyService freeEmpathyService;

    @PostMapping
    public ApiResponse<Void> createEmpathy(@Parameter(hidden = true) @LoginUser Long userId,
                                           @PathVariable Long freeId) {
        log.info("공감 컨트롤러 실행");
        freeEmpathyService.createEmpathy(userId, freeId);
        return ApiResponse.onSuccess(null);
    }

    @DeleteMapping
    public ApiResponse<Void> deleteEmpathy(@Parameter(hidden = true) @LoginUser Long userId,
                                           @PathVariable Long freeId) {
        freeEmpathyService.deleteEmpathy(userId, freeId);
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/check")
    public ApiResponse<Boolean> isEmpathized(@Parameter(hidden = true) @LoginUser Long userId,
                                             @PathVariable Long freeId) {
        return ApiResponse.onSuccess(freeEmpathyService.isEmpathized(userId, freeId));
    }

    @GetMapping("/count")
    public ApiResponse<Long> countEmpathy(@PathVariable Long freeId) {
        return ApiResponse.onSuccess(freeEmpathyService.countFreeEmpathy(freeId));
    }
}
