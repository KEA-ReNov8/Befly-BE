package befly.community.controller;

import befly.common.annotations.LoginUser;
import befly.common.apiPayload.ApiResponse;
import befly.community.service.FreeCommentService;
import befly.community.dto.FreeCommentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import befly.community.dto.CommentDto;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/community/free/{freeId}/comment")
@RequiredArgsConstructor
public class FreeCommentController {

    private final FreeCommentService freeCommentService;

    // 자유함 댓글 생성
    @PostMapping
    public ApiResponse<FreeCommentResponse> createComment(@LoginUser Long userId,
                                                          @PathVariable Long freeId,
                                                          @RequestBody CommentDto commentDto) {
        return ApiResponse.onSuccess(freeCommentService.createComment(userId, freeId, commentDto));
    }

    // 자유함 댓글 수정
    @PatchMapping("/{commentId}")
    public ApiResponse<FreeCommentResponse> updateComment(@LoginUser Long userId,
                                                          @PathVariable Long freeId,
                                                          @PathVariable Long commentId,
                                                          @RequestBody CommentDto commentDto) {
        return ApiResponse.onSuccess(freeCommentService.updateComment(userId, freeId, commentId, commentDto));
    }

    // 자유함 댓글 조회
    @GetMapping
    public ApiResponse<List<FreeCommentResponse>> getComments(@PathVariable Long freeId) {
        return ApiResponse.onSuccess(freeCommentService.getComments(freeId));
    }

    // 자유함 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> deleteComment(@LoginUser Long userId,
                                           @PathVariable Long freeId,
                                           @PathVariable Long commentId) {
        freeCommentService.deleteComment(userId, freeId, commentId);
        return ApiResponse.onSuccess(null);
    }
}
