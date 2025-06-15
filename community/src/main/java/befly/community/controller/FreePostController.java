package befly.community.controller;

import befly.common.annotations.LoginUser;
import befly.common.apiPayload.ApiResponse;
import befly.common.s3.S3Interface;
import befly.community.dto.ImageUrlsResponse;
import befly.community.dto.FreePostListResponse;
import befly.community.service.FreePostService;
import befly.community.dto.FreePostRequest;
import befly.community.dto.FreePostResponse;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/community/free")
@RequiredArgsConstructor
public class FreePostController {
    private final FreePostService freePostService;
    private final S3Interface s3Interface;
    // 자유함 글 생성
    @PostMapping
    public ApiResponse<FreePostResponse> createPost(@Parameter(hidden = true) @LoginUser Long userId,
                                                    @RequestBody FreePostRequest request) {
        return ApiResponse.onSuccess(freePostService.createPost(userId, request));
    }

    // 자유함 글 조회
    @GetMapping("/{freeId}")
    public ApiResponse<FreePostResponse> getPost(@PathVariable Long freeId) {
        return ApiResponse.onSuccess(freePostService.getPost(freeId));
    }

    // 특정 유저 아이디로 자유함 글 조회
    @GetMapping("/user/{userId}")
    public ApiResponse<List<FreePostListResponse>> getAllPostsByUserId(
            @PathVariable Long userId
    ) {
        List<FreePostListResponse> response = freePostService.getPostByUserId(userId);
        return ApiResponse.onSuccess(response);
    }

    // 자유함 글 리스트 조회 (페이지 사이즈 8, 생성 시간 순)
    @GetMapping("/page/{page}")
    public ApiResponse<Page<FreePostListResponse>> getAllPosts(
            @PathVariable int page
    ) {
        Pageable pageable = PageRequest.of(page, 8, Sort.Direction.DESC, "createdAt");
        Page<FreePostListResponse> response = freePostService.getAllPosts(pageable);
        return ApiResponse.onSuccess(response);
    }

    // 자유함 최신 글 조회
    @GetMapping("/latest")
    public ApiResponse<List<FreePostListResponse>> getLatestPost() {
        return ApiResponse.onSuccess(freePostService.getLatestFreePosts());
    }

    // 자유함 글 수정
    @PatchMapping("/{freeId}")
    public ApiResponse<FreePostResponse> updatePost(@Parameter(hidden = true) @LoginUser Long userId,
                                                    @PathVariable Long freeId,
                                                    @RequestBody FreePostRequest request) {
        return ApiResponse.onSuccess(freePostService.updatePost(userId, freeId, request));
    }

    // 자유함 글 삭제
    @DeleteMapping("/{freeId}")
    public ApiResponse<Void> deletePost(@Parameter(hidden = true) @LoginUser Long userId,
                                        @PathVariable Long freeId) {
        freePostService.deletePost(userId, freeId);
        return ApiResponse.onSuccess(null);
    }

    // 이미지 URL
    @GetMapping("/image")
    public ApiResponse<List<ImageUrlsResponse>> getImageUrls(@RequestParam List<String> imageKeys) {
        List<ImageUrlsResponse> responses = imageKeys.stream()
                .map(imageKey -> {
                    String preSignedUrl = s3Interface.createPreSignedUrl(imageKey, "PUT");
                    String path = preSignedUrl.split("\\?")[0];  // 쿼리스트링 제거
                    String fileName = path.substring(path.lastIndexOf('/') + 1);
                    return new ImageUrlsResponse(
                            fileName,
                            preSignedUrl
                    );
                })
                .toList();
        return ApiResponse.onSuccess(responses);
    }

}
