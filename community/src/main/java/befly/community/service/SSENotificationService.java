package befly.community.service;

import befly.community.dto.kafka.NotificationMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestClient;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class SSENotificationService {

    private final ObjectMapper objectMapper;

    // 사용자 ID를 키로 하는 SSE 이미터 저장소
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * 사용자 연결 등록
     * @param userId 사용자 ID
     * @return SseEmitter 객체
     */
    public SseEmitter subscribe(String userId) {
        // 타임아웃 설정 (현재는 무제한)
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        // 연결 완료 이벤트 처리
        emitter.onCompletion(() -> {
            log.info("SSE connection completed for user: {}", userId);
            removeEmitter(userId);
        });

        // 타임아웃 이벤트 처리
        emitter.onTimeout(() -> {
            log.info("SSE connection timeout for user: {}", userId);
            removeEmitter(userId);
        });

        // 에러 이벤트 처리
        emitter.onError((ex) -> {
            log.error("SSE error for user {}: {}", userId, ex.getMessage());
            removeEmitter(userId);
        });

        // 이미터 저장
        emitters.put(userId, emitter);
        log.info("New SSE connection established for user: {}", userId);

        // 연결 확인용 더미 이벤트 전송
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("Connected to notification stream"));
        } catch (IOException e) {
            log.error("Error sending initial connection event to user {}", userId, e);
            removeEmitter(userId);
        }

        return emitter;
    }

    /**
     * 특정 사용자에게 알림 전송
     */
    public void sendNotification(String userId, NotificationMessage notification) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                // 알림 메시지 전송
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(objectMapper.writeValueAsString(notification)));
                log.debug("Sent notification to user {} via SSE", userId);
            } catch (IOException e) {
                log.error("Failed to send notification to user {}", userId, e);
                removeEmitter(userId);
            }
        } else {
            log.debug("No active SSE connection for user {}", userId);
        }
    }

    /**
     * 이미터 제거
     */
    private void removeEmitter(String userId) {
        emitters.remove(userId);
        log.debug("Removed SSE emitter for user: {}", userId);
    }

    /**
     * 연결된 사용자 수 반환
     */
    public int getActiveConnectionCount() {
        return emitters.size();
    }
}