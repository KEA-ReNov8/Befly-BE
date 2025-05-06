package befly.community.service.kafka;

import befly.community.dto.kafka.NotificationMessage;
import befly.community.service.SSENotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumerService {

    private final ObjectMapper objectMapper;
    private final SSENotificationService sseNotificationService;

    @KafkaListener(
            topics = {"notification.freepost", "notification.solvedpost", "notification.like"})
    public void consumeNotification(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_KEY) String userId) {

        try {
            NotificationMessage notification = objectMapper.readValue(message, NotificationMessage.class);
            sseNotificationService.sendNotification(userId, notification);
            log.info("Sent SSE notification to user {}: {}", userId, notification.getMessage());
        } catch (Exception e) {
            log.error("Error processing Kafka message for user {}: {}", userId, e.getMessage(), e);
        }
    }
}
