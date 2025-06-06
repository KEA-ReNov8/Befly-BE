package befly.community.controller;

import befly.common.annotations.LoginUser;
import befly.common.apiPayload.ApiResponse;
import befly.community.service.SSENotificationService;
import befly.community.service.kafka.NotificationProducerService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/community")
@RequiredArgsConstructor
public class NotificationController {

    private final SSENotificationService sseNotificationService;
    private final NotificationProducerService producerService;

    /**
     * 알림이 오는 사이트
     * @param userId
     * @return
     */
    @GetMapping(value = "/noti", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@LoginUser @Parameter(hidden = true) Long userId) {
        String St_userId = userId.toString();
        log.info("SSE subscribe request from user: {}", userId);
        return sseNotificationService.subscribe(St_userId);
    }


    /**
     * 들어가면 알림보임
     */

    @GetMapping("/notification")
    public ApiResponse<List<String>> notification(@LoginUser @Parameter(hidden = true) Long userId) {
        List<String> notifications = producerService.getNotifications(userId);
        return ApiResponse.onSuccess(notifications);
    }
}
