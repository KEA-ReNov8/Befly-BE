package befly.community.controller;

import befly.common.annotations.LoginUser;
import befly.common.apiPayload.ApiResponse;
import befly.common.s3.S3Interface;
import befly.community.service.FreePostService;
import befly.community.dto.FreePostRequest;
import befly.community.dto.FreePostResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/community/free")
@RequiredArgsConstructor
public class FreePostController {
    private final FreePostService freePostService;
    private final S3Interface s3Interface;

    @GetMapping("/test")
    public ApiResponse<Long> test(@LoginUser Long userId) {
        log.info("test");
        log.info("userId:{}", userId);
        return ApiResponse.onSuccess(userId);
    }

    // 자유함 글 생성
    @PostMapping
    public ApiResponse<FreePostResponse> createPost(@LoginUser Long userId,
                                                    @RequestBody FreePostRequest request) {
        return ApiResponse.onSuccess(freePostService.createPost(userId, request));
    }

    // 자유함 글 조회
    @GetMapping("/{freeId}")
    public ApiResponse<FreePostResponse> getPost(@PathVariable Long freeId) {
        return ApiResponse.onSuccess(freePostService.getPost(freeId));
    }

    // 자유함 글 리스트 조회
    @GetMapping
    public ApiResponse<List<FreePostResponse>> getAllPosts() {
        return ApiResponse.onSuccess(freePostService.getAllPosts());
    }

    // 자유함 글 수정
    @PatchMapping("/{freeId}")
    public ApiResponse<FreePostResponse> updatePost(@LoginUser Long userId,
                                                    @PathVariable Long freeId,
                                                    @RequestBody FreePostRequest request) {
        return ApiResponse.onSuccess(freePostService.updatePost(userId, freeId, request));
    }

    // 자유함 글 삭제
    @DeleteMapping("/{freeId}")
    public ApiResponse<Void> deletePost(@LoginUser Long userId,
                                        @PathVariable Long freeId) {
        freePostService.deletePost(userId, freeId);
        return ApiResponse.onSuccess(null);
    }

    // Pre-signed URL 생성
    @PostMapping("/presigned")
    public ApiResponse<String> getPresignedUrl(@RequestParam String imageKey) {
        return ApiResponse.onSuccess(s3Interface.createPreSignedUrl(imageKey, "PUT"));
    }

    // 업로드된 이미지 URL 조회
    @GetMapping("/url")
    public ApiResponse<String> getImageUrl(@RequestParam String imageKey) {
        return ApiResponse.onSuccess(s3Interface.getImageUrl(imageKey));
    }

}
