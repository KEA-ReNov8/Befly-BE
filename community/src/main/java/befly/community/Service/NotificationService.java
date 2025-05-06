package befly.community.service;

import befly.common.code.status.GlobalErrorStatus;
import befly.common.exception.RestApiException;
import befly.community.Repository.FreeCommentRepository;
import befly.community.Repository.FreePostRepository;
import befly.community.domain.comment.FreeComment;
import befly.community.dto.CommentDto;
import befly.community.dto.kafka.NotificationMessage;
import befly.community.dto.kafka.NotificationType;
import befly.community.service.kafka.KafkaNotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NotificationService {
    private final FreeCommentRepository commentRepository;
    private final FreePostRepository freePostRepository;
    private final KafkaNotificationService kafkaNotificationService;


    public void createComment(CommentDto commentDto) {
//        자유함인 경우 알림 발송
        switch (commentDto.getNotificationType()) {
            case FREEPOST -> {
                // 게시글 작성자 ID 조회
                long postUserId = freePostRepository.findById(commentDto.getPostId())
                        .orElseThrow(() -> new RestApiException(GlobalErrorStatus.INVALID_POST)).getUserId();


//                댓글 저장
                FreeComment comment = FreeComment.builder()
                        .freeId(
                        freePostRepository.findById(commentDto.getPostId()).orElseThrow(
                                () -> new RestApiException(GlobalErrorStatus.INVALID_POST))
                        )
                        .userId(commentDto.getUserId())
                        .isDeleted(false)
                        .freeComment(commentDto.getComment())
                        .pFreeCommentId(
                                commentDto.getPcommentId() != null ?
                                        commentRepository.findById(commentDto.getPcommentId())
                                                .orElseThrow(() -> new RestApiException(GlobalErrorStatus.INVALID_COMMENT)) : null
                        )
                        .build();
                commentRepository.save(comment);

                // 본인이 작성한 게시글에 댓글을 작성한 경우 알림 발송하지 않음
//                즉, 만약 본인이 작성한 게시글일 경우 아래 코드 발생안한다는 것 (바로 아래 조건문이 그거 확인하는 용도)
                if (postUserId != commentDto.getUserId()) {
                    NotificationMessage message = NotificationMessage.builder()
                            .targetUserId(postUserId)
                            .senderId(commentDto.getUserId())
                            .senderUsername("TestCommenterNickName")  // TODO: USERDB에서 가져오기 혹은 USER에서 요청하기
                            .type(NotificationType.FREEPOST)
                            .message("TestCommenterNickName님이 댓글을 달았습니다.")
                            .createdAt(LocalDateTime.now())
                            .build();
                    log.info("Sending notification to user {}: {}", postUserId, message);

                    kafkaNotificationService.sendNotification(message);
                }
            }

            case SOLVEDPOST-> {
                // 자유함 답글 알림 로직 구현
            }

            case LIKE -> {
                // 좋아요 알림 로직 구현
            }
            default -> {
                // 기본 케이스 처리 오류인경우
            }
        }
    }
}