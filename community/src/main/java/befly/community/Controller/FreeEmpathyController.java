package befly.community.Controller;

import befly.common.annotations.LoginUser;
import befly.common.apiPayload.ApiResponse;
import befly.community.Service.FreeEmpathyService;
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
    public ApiResponse<Void> createEmpathy(@LoginUser Long userId,
                                           @PathVariable Long freeId) {
        freeEmpathyService.createEmpathy(userId, freeId);
        return ApiResponse.onSuccess(null);
    }

    @DeleteMapping
    public ApiResponse<Void> deleteEmpathy(@LoginUser Long userId,
                                           @PathVariable Long freeId) {
        freeEmpathyService.deleteEmpathy(userId, freeId);
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/check")
    public ApiResponse<Boolean> isEmpathized(@LoginUser Long userId,
                                             @PathVariable Long freeId) {
        return ApiResponse.onSuccess(freeEmpathyService.isEmpathized(userId, freeId));
    }

    @GetMapping("/count")
    public ApiResponse<Long> countEmpathy(@PathVariable Long freeId) {
        return ApiResponse.onSuccess(freeEmpathyService.countFreeEmpathy(freeId));
    }
}
