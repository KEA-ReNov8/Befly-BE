package befly.community.controller;

import befly.common.apiPayload.ApiResponse;
import befly.community.dto.CommentDto;
import befly.community.service.NotificationService;
import befly.community.service.SSENotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SSENotificationService sseNotificationService;

    /**
     * 알림이 오는 사이트
     * @param userId
     * @return
     */
    @GetMapping(value = "/noti/{user_id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable("user_id") String userId) {
        //TODO 요청한 유저가 실제 로그인한 유저와 같은지 검증 필요
        log.info("SSE subscribe request from user: {}", userId);
        return sseNotificationService.subscribe(userId);
    }

    /**
     * 댓글 달면 여기로 POST
     * @param commentDto
     * @return
     */
    @PostMapping("/free/comment")
    public ApiResponse<?> createFreeComment(@RequestBody CommentDto commentDto) {
        notificationService.createComment(commentDto);
        return ApiResponse.onSuccess("성공");
    }

}
