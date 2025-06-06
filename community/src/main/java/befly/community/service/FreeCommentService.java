package befly.community.service;

import befly.common.apiPayload.ApiResponse;
import befly.common.exception.RestApiException;
import befly.community.client.UserServiceClient;
import befly.community.repository.FreeCommentRepository;
import befly.community.repository.FreePostRepository;
import befly.community.domain.FreePost;
import befly.community.domain.comment.FreeComment;
import befly.community.dto.CommentDto;
import befly.community.dto.FreeCommentResponse;
import befly.community.dto.kafka.NotificationType;
import befly.community.service.kafka.NotificationProducerService;
import befly.community.status.FreeErrorStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FreeCommentService {
    private final FreeCommentRepository freeCommentRepository;
    private final FreePostRepository freePostRepository;
    private final NotificationProducerService notificationProducerService;
    private final UserServiceClient userServiceClient;

    // 자유함 댓글 생성
    @Transactional
    public FreeCommentResponse createComment(Long userId, Long freeId, CommentDto commentDto) {
        FreePost post = freePostRepository.findById(freeId)
                .orElseThrow(() -> new RestApiException(FreeErrorStatus.POST_NOT_FOUND));

        System.out.println(commentDto);
        FreeComment pComment = null;
        System.out.println("PCommentID : " + commentDto.getPcommentId());
        if (commentDto.getPcommentId() != null) {
            pComment = freeCommentRepository.findById(commentDto.getPcommentId())
                    .filter(comment -> !comment.getIsDeleted())
                    .filter(comment -> comment.getFreeId().getFreeId().equals(freeId))
                    .orElseThrow(() -> new RestApiException(FreeErrorStatus.COMMENT_NOT_FOUND));
        }

        FreeComment comment = FreeComment.builder()
                .freeId(post)
                .userId(userId)
                .isDeleted(false)
                .freeComment(commentDto.getComment())
                .pFreeCommentId(pComment)
                .build();
        FreeComment saved = freeCommentRepository.save(comment);

        freePostRepository.findById(freeId)
                .ifPresent(freePost -> {
                    Long postUserId = freePost.getUserId();
                    // 알림을 보내는 조건 (postUserId가 현재 사용자 userId와 다른 경우)도 여기서 처리
                    if (postUserId != null && !postUserId.equals(userId)) { // null 체크 및 본인에게 알림 보내지 않기
                        notificationProducerService.sendNotificationIfNeeded(postUserId, userId, NotificationType.FREEPOST);
                    }
                });

        return toResponse(saved);
    }

    // 자유함 댓글 업데이트
    @Transactional
    public FreeCommentResponse updateComment(Long userId, Long freeId, Long commentId, CommentDto commentDto) {
        FreeComment comment = freeCommentRepository.findById(commentId)
                .filter(c -> !c.getIsDeleted())
                .filter(c -> c.getFreeId().getFreeId().equals(freeId))
                .orElseThrow(() -> new RestApiException(FreeErrorStatus.COMMENT_NOT_FOUND));

        // 작성자 본인 확인
        if (!comment.getUserId().equals(userId)) {
            throw new RestApiException(FreeErrorStatus.NO_PERMISSION);
        }

        comment.updateFreeComment(commentDto.getComment());

        // FreeComment updated = freeCommentRepository.save(comment);
        return toResponse(comment);
    }

    // 자유함 댓글 리스트 조회
    public List<FreeCommentResponse> getComments(Long freeId) {
        FreePost freePost = freePostRepository.findById(freeId)
                .orElseThrow(() -> new RestApiException(FreeErrorStatus.POST_NOT_FOUND));

        return freeCommentRepository.findByFreeId(freePost).stream()
                .map(comment -> FreeCommentResponse.builder()
                        .commentId(comment.getFreeCommentId())
                        .postId(comment.getFreeId())
                        // .userId(comment.getUserId())
                        .nickname(userServiceClient.getUserNicknameById(comment.getUserId()).getResult())
                        .comment(comment.getIsDeleted() ? "삭제된 댓글입니다." : comment.getFreeComment())
                        .parentCommentId(comment.getPFreeCommentId())
                        .isDeleted(comment.getIsDeleted())
                        .createdAt(comment.getCreatedAt())
                        .updatedAt(comment.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // 자유함 댓글 삭제
    @Transactional
    public void deleteComment(Long userId, Long freeId, Long commentId) {
        FreeComment comment = freeCommentRepository.findById(commentId)
                .filter(c -> !c.getIsDeleted())
                .filter(c -> c.getFreeId().getFreeId().equals(freeId))
                .orElseThrow(() -> new RestApiException(FreeErrorStatus.COMMENT_NOT_FOUND));

        // 작성자 본인 확인
        if (!comment.getUserId().equals(userId)) {
            throw new RestApiException(FreeErrorStatus.NO_PERMISSION);
        }

        comment.deleteFreeComment();

        freeCommentRepository.save(comment);
    }


    // 결과 응답용
    private FreeCommentResponse toResponse(FreeComment comment) {
        ApiResponse<String> responseWithNickname = userServiceClient.getUserNicknameById(comment.getUserId());
        String nickname = responseWithNickname.getResult();

        return FreeCommentResponse.builder()
                .commentId(comment.getFreeCommentId())
                .postId(comment.getFreeId())
                // .userId(comment.getUserId())
                .nickname(nickname)
                .comment(comment.getFreeComment())
                .isDeleted(comment.getIsDeleted())
                .parentCommentId(comment.getPFreeCommentId())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
