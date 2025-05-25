package befly.community.controller;

import befly.common.annotations.LoginUser;
import befly.common.apiPayload.ApiResponse;
import befly.community.service.SolvedEmpathyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/solved/{solvedId}/empathy")
@RequiredArgsConstructor
public class SolvedEmpathyController {

    private final SolvedEmpathyService solvedEmpathyService;

    //해결함 공감 생성
    @PostMapping
    public ApiResponse<Void> createEmpathy(@LoginUser Long userId,
                                           @PathVariable Long solvedId) {
        solvedEmpathyService.createEmpathy(userId, solvedId);
        return ApiResponse.onSuccess(null);
    }

    // 해결함 공감 취소
    @DeleteMapping
    public ApiResponse<Void> deleteEmpathy(@LoginUser Long userId,
                                           @PathVariable Long solvedId) {
        solvedEmpathyService.deleteEmpathy(userId, solvedId);
        return ApiResponse.onSuccess(null);
    }

    // 유저가 공감했는지 여부
    @GetMapping("/check")
    public ApiResponse<Boolean> isEmpathized(@LoginUser Long userId,
                                             @PathVariable Long solvedId) {
        return ApiResponse.onSuccess(solvedEmpathyService.isEmpathized(userId, solvedId));
    }

    // 해결함 글 공감 갯수 확인
    @GetMapping("/count")
    public ApiResponse<Long> countEmpathy(@PathVariable Long solvedId) {
        return ApiResponse.onSuccess(solvedEmpathyService.countSolvedEmpathy(solvedId));
    }
}
