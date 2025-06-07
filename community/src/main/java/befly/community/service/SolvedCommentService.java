package befly.community.service;

import befly.common.exception.RestApiException;
import befly.community.domain.comment.FreeComment;
import befly.community.dto.UserProfileResponse;
import befly.community.repository.SolvedCommentRepository;
import befly.community.repository.SolvedPostRepository;
import befly.community.domain.SolvedPost;
import befly.community.domain.comment.SolvedComment;
import befly.community.dto.CommentDto;
import befly.community.dto.SolvedCommentResponse;
import befly.community.dto.kafka.NotificationType;
import befly.community.service.kafka.NotificationProducerService;
import befly.community.status.SolvedErrorStatus;
import befly.community.util.CacheUtils;
import jakarta.transaction.Transactional;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SolvedCommentService {
    private final SolvedCommentRepository solvedCommentRepository;
    private final SolvedPostRepository solvedPostRepository;
    private final NotificationProducerService notificationProducerService;
    private final CacheUtils cacheUtils;

    // 해결함 댓글 생성
    @Transactional
    public SolvedCommentResponse createComment(Long userId, Long solvedId, CommentDto commentDto) {
        SolvedPost post = solvedPostRepository.findById(solvedId)
                .orElseThrow(() -> new RestApiException(SolvedErrorStatus.POST_NOT_FOUND));

        SolvedComment pComment = null;
        if (commentDto.getPcommentId() != null) {
            pComment = solvedCommentRepository.findById(commentDto.getPcommentId())
                    .filter(comment -> !comment.getIsDeleted())
                    .filter(comment -> comment.getSolvedId().getSolvedId().equals(solvedId))
                    .orElseThrow(() -> new RestApiException(SolvedErrorStatus.COMMENT_NOT_FOUND));
        }

        SolvedComment comment = SolvedComment.builder()
                .solvedId(post)
                .userId(userId)
                .isDeleted(false)
                .solvedComment(commentDto.getComment())
                .pSolvedCommentId(pComment)
                .build();
        SolvedComment saved = solvedCommentRepository.save(comment);

        Long postUserId = post.getUserId();
        // 알림을 보내는 조건 (postUserId가 현재 사용자 userId와 다른 경우)도 여기서 처리
        if (postUserId != null && !postUserId.equals(userId)) { // null 체크 및 본인에게 알림 보내지 않기
            notificationProducerService.sendNotificationIfNeeded(postUserId, userId, NotificationType.SOLVEDPOST);
        }

        return toResponse(saved);
    }

    // 해결함 댓글 수정
    @Transactional
    public SolvedCommentResponse updateComment(Long userId, Long solvedId, Long commentId, CommentDto commentDto) {
        SolvedComment comment = solvedCommentRepository.findById(commentId)
                .filter(c -> !c.getIsDeleted())
                .filter(c -> c.getSolvedId().getSolvedId().equals(solvedId))
                .orElseThrow(() -> new RestApiException(SolvedErrorStatus.COMMENT_NOT_FOUND));

        // 작성자 본인 확인
        if (!comment.getUserId().equals(userId)) {
            throw new RestApiException(SolvedErrorStatus.NO_PERMISSION);
        }

        comment = SolvedComment.builder()
                .solvedCommentId(comment.getSolvedCommentId())
                .solvedId(comment.getSolvedId())
                .userId(comment.getUserId())
                .isDeleted(false)
                .solvedComment(commentDto.getComment())
                .pSolvedCommentId(comment.getPSolvedCommentId())
                .build();
        solvedCommentRepository.save(comment);
        return null;
    }

    // 해결함 댓글 리스트 조회
    public List<SolvedCommentResponse> getComments(Long solvedId) {
        SolvedPost solvedPost = solvedPostRepository.findById(solvedId)
                .orElseThrow(() -> new RestApiException(SolvedErrorStatus.POST_NOT_FOUND));
        List<SolvedComment> solvedCommentList = solvedCommentRepository.findBySolvedId(solvedPost);
        Map<Long, UserProfileResponse> userProfileResponseMap = cacheUtils.getUserNickName(
                solvedCommentList.stream()
                        .map(SolvedComment::getUserId)
                        .distinct()
                        .toList()
        );

        return solvedCommentRepository.findBySolvedId(solvedPost).stream()
                .map(comment -> SolvedCommentResponse.builder()
                        .commentId(comment.getSolvedCommentId())
                        .badge(userProfileResponseMap.get(comment.getUserId()).getBadge())
                        .nickname(userProfileResponseMap.get(comment.getUserId()).getNickName())
                        .postId(comment.getSolvedId())
                        .userId(comment.getUserId())
                        .profileImage(userProfileResponseMap.get(comment.getUserId()).getProfileImg())
                        .comment(comment.getIsDeleted() ? "삭제된 댓글입니다." : comment.getSolvedComment())
                        .parentCommentId(comment.getPSolvedCommentId())
                        .isDeleted(comment.getIsDeleted())
                        .createdAt(comment.getCreatedAt())
                        .updatedAt(comment.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // 해결함 댓글 삭제
    @Transactional
    public void deleteComment(Long userId, Long solvedId, Long commentId) {
        SolvedComment comment = solvedCommentRepository.findById(commentId)
                .filter(c -> !c.getIsDeleted())
                .filter(c -> c.getSolvedId().getSolvedId().equals(solvedId))
                .orElseThrow(() -> new RestApiException(SolvedErrorStatus.COMMENT_NOT_FOUND));

        // 작성자 본인 확인
        if (!comment.getUserId().equals(userId)) {
            throw new RestApiException(SolvedErrorStatus.NO_PERMISSION);
        }

        comment = SolvedComment.builder()
                .solvedCommentId(comment.getSolvedCommentId())
                .solvedId(comment.getSolvedId())
                .userId(comment.getUserId())
                .isDeleted(true)
                .solvedComment(comment.getSolvedComment())
                .pSolvedCommentId(comment.getPSolvedCommentId())
                .build();
        solvedCommentRepository.save(comment);
    }



    // 결과 응답용
    private SolvedCommentResponse toResponse(SolvedComment comment) {
        return SolvedCommentResponse.builder()
                .commentId(comment.getSolvedCommentId())
                .postId(comment.getSolvedId())
                .userId(comment.getUserId())
                .comment(comment.getSolvedComment())
                .isDeleted(comment.getIsDeleted())
                .parentCommentId(comment.getPSolvedCommentId())
                .build();
    }
}
