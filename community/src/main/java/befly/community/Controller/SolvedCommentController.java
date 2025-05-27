package befly.community.controller;

import befly.common.annotations.LoginUser;
import befly.common.apiPayload.ApiResponse;
import befly.community.service.SolvedCommentService;
import befly.community.dto.SolvedCommentResponse;
import befly.community.dto.CommentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/community/solved/{solvedId}/comment")
@RequiredArgsConstructor
public class SolvedCommentController {

    private final SolvedCommentService solvedCommentService;

    // 해결함 댓글 생성
    @PostMapping
    public ApiResponse<SolvedCommentResponse> createComment(@LoginUser Long userId,
                                                            @PathVariable Long solvedId,
                                                            @RequestBody CommentDto commentDto) {
        return ApiResponse.onSuccess(solvedCommentService.createComment(userId, solvedId, commentDto));
    }

    // 해결함 댓글 수정
    @PatchMapping("/{commentId}")
    public ApiResponse<SolvedCommentResponse> updateComment(@LoginUser Long userId,
                                                            @PathVariable Long solvedId,
                                                            @PathVariable Long commentId,
                                                            @RequestBody CommentDto commentDto) {
        return ApiResponse.onSuccess(solvedCommentService.updateComment(userId, solvedId, commentId, commentDto));
    }

    // 해결함 댓글 조회
    @GetMapping
    public ApiResponse<List<SolvedCommentResponse>> getComments(@PathVariable Long solvedId) {
        return ApiResponse.onSuccess(solvedCommentService.getComments(solvedId));
    }

    // 해결함 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> deleteComment(@LoginUser Long userId,
                                           @PathVariable Long solvedId,
                                           @PathVariable Long commentId) {
        solvedCommentService.deleteComment(userId, solvedId, commentId);
        return ApiResponse.onSuccess(null);
    }
}
