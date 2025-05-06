package befly.community.dto.kafka;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class
NotificationMessage {
    // 알림을 받을 대상 사용자 ID
    private Long targetUserId;

    // 알림 타입
    private NotificationType type;

    // 알림 발신자 (댓글 작성자)
    private Long senderId;
    private String senderUsername;

    // 알림 내용
    private String message;

    // 타임스탬프
    private LocalDateTime createdAt;
}

