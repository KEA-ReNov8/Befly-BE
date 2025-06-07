package befly.community.controller;

import befly.common.annotations.LoginUser;
import befly.common.apiPayload.ApiResponse;
import befly.community.service.kafka.NotificationProducerService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/community")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationProducerService producerService;


    /**
     * 들어가면 알림보임
     */

    @GetMapping("/notification")
    public ApiResponse<List<String>> notification(@LoginUser @Parameter(hidden = true) Long userId) {
        List<String> notifications = producerService.getNotifications(userId);
        return ApiResponse.onSuccess(notifications);
    }
}
