package befly.community.service;

import befly.common.exception.RestApiException;
import befly.community.dto.UserProfileResponse;
import befly.community.repository.FreeCommentRepository;
import befly.community.repository.FreePostRepository;
import befly.community.domain.FreePost;
import befly.community.domain.comment.FreeComment;
import befly.community.dto.CommentDto;
import befly.community.dto.FreeCommentResponse;
import befly.community.dto.kafka.NotificationType;
import befly.community.service.kafka.NotificationProducerService;
import befly.community.status.FreeErrorStatus;
import befly.community.util.CacheUtils;
import jakarta.transaction.Transactional;
import java.util.Map;
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
    private final CacheUtils cacheUtils;

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
        freeCommentRepository.save(comment);

        if (post.getUserId() != null && !post.getUserId().equals(userId)) { // null 체크 및 본인에게 알림 보내지 않기
            notificationProducerService.sendNotificationIfNeeded(post.getUserId(), userId, NotificationType.FREEPOST);
        }

        return null;
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
        return null;
    }

    // 자유함 댓글 리스트 조회
    public List<FreeCommentResponse> getComments(Long freeId) {
        FreePost freePost = freePostRepository.findById(freeId)
                .orElseThrow(() -> new RestApiException(FreeErrorStatus.POST_NOT_FOUND));
        List<FreeComment> freeCommentList = freeCommentRepository.findByFreeId(freePost);

        Map<Long, UserProfileResponse> userProfileResponseMap = cacheUtils.getUserNickName(
                freeCommentList.stream()
                        .map(FreeComment::getUserId)
                        .distinct()
                        .toList()
        );

        return freeCommentRepository.findByFreeId(freePost).stream()
                .map(comment -> FreeCommentResponse.builder()
                        .commentId(comment.getFreeCommentId())
                        .postId(comment.getFreeId())
                        .badge(userProfileResponseMap.get(comment.getUserId()).getBadge())
                        .userId(userProfileResponseMap.get(comment.getUserId()).getUserId())
                        .nickname(userProfileResponseMap.get(comment.getUserId()).getNickName())
                        .profileImage(userProfileResponseMap.get(comment.getUserId()).getProfileImg())
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
}
