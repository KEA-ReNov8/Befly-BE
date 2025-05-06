package befly.community.service.kafka;


import befly.community.dto.kafka.NotificationMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaNotificationService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendNotification(NotificationMessage message) {
        try {
            String topic = "notification." + message.getType().name().toLowerCase();
            log.info("Sending Kafka message to topic {}", topic);
            String json = objectMapper.writeValueAsString(message);
            kafkaTemplate.send(topic, String.valueOf(message.getTargetUserId()), json);
            log.info("Sent Kafka message to topic {}: {}", topic, json);
        } catch (Exception e) {
            log.error("Failed to send Kafka message", e);
        }
    }
}
