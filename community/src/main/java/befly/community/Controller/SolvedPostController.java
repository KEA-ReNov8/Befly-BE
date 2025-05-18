package befly.community.Controller;

import befly.common.annotations.LoginUser;
import befly.common.apiPayload.ApiResponse;
import befly.common.s3.S3Interface;
import befly.community.Service.SolvedPostService;
import befly.community.dto.SolvedPostRequest;
import befly.community.dto.SolvedPostResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/solved")
@RequiredArgsConstructor
public class SolvedPostController {
    private final SolvedPostService solvedPostService;
    private final S3Interface s3Interface;

    @GetMapping("/test")
    public ApiResponse<Long> test(@LoginUser Long userId) {
        log.info("test");
        log.info("userId:{}", userId);
        return ApiResponse.onSuccess(userId);
    }

    // 해결함 글 생성
    @PostMapping
    public ApiResponse<SolvedPostResponse> createPost(@LoginUser Long userId,
                                                      @RequestBody SolvedPostRequest request) {
        return ApiResponse.onSuccess(solvedPostService.createPost(userId, request));
    }
}
