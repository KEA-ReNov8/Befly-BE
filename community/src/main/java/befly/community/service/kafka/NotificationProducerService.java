package befly.community.service.kafka;

import befly.common.apiPayload.ApiResponse;
import befly.community.client.UserServiceClient;
import befly.community.domain.Notification;
import befly.community.dto.kafka.NotificationMessage;
import befly.community.dto.kafka.NotificationType;
import befly.community.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NotificationProducerService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final UserServiceClient userServiceClient;
    private final NotificationRepository notificationRepository;

    // 알림 발송 (본인이 작성한 게시글에 댓글을 단 경우 제외), postUserId는 게시글 주인, getuserId는 뭔가를 한 작성자,즉 로그인한 유저 마지막은 유형
    public void sendNotificationIfNeeded(long postUserId, long commentUserId, NotificationType type) {
        // 본인이 작성한 게시글에 댓글을 작성한 경우 알림 발송하지 않음, 좋아요도 마찬가지
        ApiResponse<String> responseWithNickname = userServiceClient.getUserNicknameById(commentUserId);
        String Nickname = responseWithNickname.getResult();

        String messageContent = switch (type) {
            case FREELIKE -> Nickname + "님이 자유함을 좋아합니다.";
            case SOLVEDLIKE ->  Nickname + "님이 공유함을 좋아합니다.";
            case FREEPOST -> Nickname + "님의 자유함에 댓글을 남겼습니다.";
            case SOLVEDPOST -> Nickname +"님이 회원님의 공유함에 댓글을 남겼습니다.";
            default -> "알 수 없는 알림입니다. 서버에 문의하세요";
        };


        if (postUserId != commentUserId) {
            NotificationMessage message = NotificationMessage.builder()
                    .targetUserId(postUserId)
                    .senderId(commentUserId)
                    .senderUsername(Nickname)
                    .type(type) //여기에 들어가는 타입이 토픽에 들어감
                    .message(messageContent)
                    .createdAt(LocalDateTime.now())
                    .build();


            Notification notification = Notification.builder()
                    .userId(postUserId)
                    .message(messageContent)
                    .build();
            notificationRepository.save(notification);
        }
    }


    public List<String> getNotifications(Long userId) {
        log.info("{}", userId);
        List<Notification> userNotifications = notificationRepository.findByUserId(userId);
        List<String> messages = new ArrayList<>();
        for (Notification notification : userNotifications) {
            messages.add(notification.getMessage()); // 각 Notification 객체의 메시지를 리스트에 추가합니다.
        }
        return messages;
    }
}