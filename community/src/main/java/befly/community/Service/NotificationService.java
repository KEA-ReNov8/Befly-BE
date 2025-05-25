package befly.community.Service;

import befly.common.code.status.GlobalErrorStatus;
import befly.common.exception.RestApiException;
import befly.community.Repository.FreeCommentRepository;
import befly.community.Repository.FreePostRepository;
import befly.community.Repository.SolvedCommentRepository;
import befly.community.Repository.SolvedPostRepository;
import befly.community.domain.FreePost;
import befly.community.domain.SolvedPost;
import befly.community.domain.comment.FreeComment;
import befly.community.domain.comment.SolvedComment;
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
    private final FreeCommentRepository freeCommentRepository;
    private final FreePostRepository freePostRepository;
    private final KafkaNotificationService kafkaNotificationService;
    private final SolvedPostRepository solvedPostRepository;
    private final SolvedCommentRepository solvedCommentRepository;

    public void createComment(CommentDto commentDto) {
        switch (commentDto.getNotificationType()) {
            case FREEPOST -> handleFreePostComment(commentDto);
            case SOLVEDPOST -> handleSolvedPostComment(commentDto);
            //TODO 자기 글에는 자기가 좋아요를 누를 수 있나???
            case SOVLEDLIKE -> handleSolvedLike(commentDto);
            case FREELIKE -> handleFreeLike(commentDto);
            default -> {
                throw new RestApiException(GlobalErrorStatus.INVALID_NOTI_TYPE);
            }
        }
    }

    private void handleSolvedLike(CommentDto commentDto) {


    }

    private void handleFreeLike(CommentDto commentDto) {

    }

    private void handleFreePostComment(CommentDto commentDto) {
        // 게시글 작성자 ID 조회
        FreePost post = freePostRepository.findById(commentDto.getPostId())
                .orElseThrow(() -> new RestApiException(GlobalErrorStatus.INVALID_POST));
        long postUserId = post.getUserId();

        // 댓글 저장
        FreeComment comment = FreeComment.builder()
                .freeId(post)
                .userId(commentDto.getUserId())
                .isDeleted(false)
                .freeComment(commentDto.getComment())
                .pFreeCommentId(getParentFreeComment(commentDto.getPcommentId()))
                .build();
        freeCommentRepository.save(comment);

        // 알림 발송 (본인이 작성한 게시글에 댓글을 단 경우 제외)
        sendNotificationIfNeeded(postUserId, commentDto.getUserId(), NotificationType.FREEPOST);
    }

    private void handleSolvedPostComment(CommentDto commentDto) {
        // 게시글 작성자 ID 조회
        SolvedPost post = solvedPostRepository.findById(commentDto.getPostId())
                .orElseThrow(() -> new RestApiException(GlobalErrorStatus.INVALID_POST));
        long postUserId = post.getUserId();

        // 댓글 저장
        SolvedComment comment = SolvedComment.builder()
                .solvedId(post)
                .userId(commentDto.getUserId())
                .isDeleted(false)
                .solvedComment(commentDto.getComment())
                .pSolvedCommentId(getParentSolvedComment(commentDto.getPcommentId()))
                .build();
        solvedCommentRepository.save(comment);

        sendNotificationIfNeeded(postUserId, commentDto.getUserId(), NotificationType.SOLVEDPOST);
    }

    private FreeComment getParentFreeComment(Long pCommentId) {
        if (pCommentId == null) return null;
        return freeCommentRepository.findById(pCommentId)
                .orElseThrow(() -> new RestApiException(GlobalErrorStatus.INVALID_COMMENT));
    }

    private SolvedComment getParentSolvedComment(Long pCommentId) {
        if (pCommentId == null) return null;
        return solvedCommentRepository.findById(pCommentId)
                .orElseThrow(() -> new RestApiException(GlobalErrorStatus.INVALID_COMMENT));
    }


    // 알림 발송 (본인이 작성한 게시글에 댓글을 단 경우 제외), postUserId는 게시글 주인, getuserId는 뭔가를 한 작성자,즉 로그인한 유저 마지막은 유형
    public void sendNotificationIfNeeded(long postUserId, long commentUserId, NotificationType type) {
        // 본인이 작성한 게시글에 댓글을 작성한 경우 알림 발송하지 않음, 좋아요도 마찬가지
        //TODO
//        추후 댓글 작성자의 닉네임을 가져올 수 있도록 수정


        String messageContent = switch (type) {
            case FREELIKE -> "TestCommenterNickName" + "님이 자유함을 좋아합니다.";
            case SOVLEDLIKE ->  "TestCommenterNickName" + "님이 해결함을 좋아합니다.";
            case FREEPOST -> "TestCommenterNickName" + "님의 자유함에 댓글을 남겼습니다.";
            case SOLVEDPOST -> "TestCommenterNickName" +"님이 회원님의 해결함에 댓글을 남겼습니다.";
            default -> "알 수 없는 알림입니다. 서버에 문의하세요";
        };


        if (postUserId != commentUserId) {
            NotificationMessage message = NotificationMessage.builder()
                    .targetUserId(postUserId)
                    .senderId(commentUserId)
                    .senderUsername("TestCommenterNickName")
                    .type(type) //여기에 들어가는 타입이 토픽에 들어감
                    .message(messageContent)
                    .createdAt(LocalDateTime.now())
                    .build();

            kafkaNotificationService.sendNotification(message);
        }
    }
}