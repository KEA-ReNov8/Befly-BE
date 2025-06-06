package befly.community.service.kafka;

import befly.common.common.WingMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WingEventProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void produceWingEvent(Long userId, Long wing) {
        log.info("[WingEventProducerService] produceWingEvent 실행");

        WingMessage message = new WingMessage(userId, wing);

        // __TypeId__ 헤더를 추가하여 역직렬화 대상 타입 명시
        Message<WingMessage> kafkaMessage = MessageBuilder
                .withPayload(message)
                .setHeader(KafkaHeaders.TOPIC, "user-wing")
                .setHeader("__TypeId__", "befly.common.common.WingMessage")
                .build();

        kafkaTemplate.send(kafkaMessage);
        log.info("[WingEventProducerService] 토픽 발행 완료: {}", message);
    }
}
