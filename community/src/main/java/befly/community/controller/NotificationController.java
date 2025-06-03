package befly.community.controller;

import befly.common.annotations.LoginUser;
import befly.community.service.SSENotificationService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("/community")
@RequiredArgsConstructor
public class NotificationController {

    private final SSENotificationService sseNotificationService;

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
}
