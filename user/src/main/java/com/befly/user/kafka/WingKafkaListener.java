package com.befly.user.kafka;

import com.befly.user.dto.WingMessage;
import com.befly.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WingKafkaListener {

    private final UserService userService;

    @KafkaListener(topics = "user-wing", groupId = "user-service")
    public void listenWingMessage(WingMessage message) {
        log.info("Received wing message: {}", message);
        try {
            userService.addWing(message.getUserId(), message.getWing());
            log.info("Successfully added wing to user: {}", message.getUserId());
        } catch (Exception e) {
            log.error("Error processing wing message: {}", e.getMessage(), e);
        }
    }
} 