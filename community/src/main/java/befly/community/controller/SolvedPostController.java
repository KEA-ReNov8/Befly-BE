package befly.community.controller;

import befly.common.annotations.LoginUser;
import befly.common.apiPayload.ApiResponse;
import befly.community.service.SolvedPostService;
import befly.community.dto.SolvedPostRequest;
import befly.community.dto.SolvedPostResponse;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/community/solved")
@RequiredArgsConstructor
public class SolvedPostController {
    private final SolvedPostService solvedPostService;

    // 해결함 글 생성
    @PostMapping
    public ApiResponse<SolvedPostResponse> createPost(
            @Parameter(hidden = true) @LoginUser Long userId,
            @RequestBody SolvedPostRequest request) {
        return ApiResponse.onSuccess(solvedPostService.createPost(userId, request));
    }

    // 해결함 글 수정
    @PatchMapping("/{solvedId}")
    public ApiResponse<SolvedPostResponse> updatePost(
            @Parameter(hidden = true) @LoginUser Long userId,
            @PathVariable Long solvedId,
            @RequestBody SolvedPostRequest request) {
        return ApiResponse.onSuccess(solvedPostService.updatePost(userId, solvedId, request));
    }

    // 해결함 글 단건 조회
    @GetMapping("/{solvedId}")
    public ApiResponse<SolvedPostResponse> getPost(@PathVariable Long solvedId) {
        return ApiResponse.onSuccess(solvedPostService.getPost(solvedId, null));
    }

    // 최신글 4개 조회
    @GetMapping("/latest")
    public ApiResponse<List<SolvedPostResponse>> getLatestPosts() {
        return ApiResponse.onSuccess(solvedPostService.getLatestPosts(null));
    }

    // 페이지네이션 (페이지 사이즈 8, 생성일순)

    @GetMapping("/page/{page}")
    public ResponseEntity<ApiResponse<Page<SolvedPostResponse>>> getAllPosts(@PathVariable int page) {
        Pageable pageable = PageRequest.of(page, 8, Sort.Direction.DESC, "createdAt");
        Page<SolvedPostResponse> response = solvedPostService.getAllPosts(null, pageable);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    // 해결함 글 삭제
    @DeleteMapping("/{solvedId}")
    public ApiResponse<Void> deletePost(
            @Parameter(hidden = true) @LoginUser Long userId,
            @PathVariable Long solvedId) {
        solvedPostService.deletePost(userId, solvedId);
        return ApiResponse.onSuccess(null);
    }
}
