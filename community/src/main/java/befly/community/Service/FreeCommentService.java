package befly.community.Service;

import befly.common.exception.RestApiException;
import befly.community.Repository.FreeCommentRepository;
import befly.community.Repository.FreePostRepository;
import befly.community.domain.FreePost;
import befly.community.domain.comment.FreeComment;
import befly.community.dto.CommentDto;
import befly.community.dto.FreeCommentResponse;
import befly.community.status.FreeErrorStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FreeCommentService {
    private final FreeCommentRepository freeCommentRepository;
    private final FreePostRepository freePostRepository;

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

        comment = FreeComment.builder()
                .freeCommentId(comment.getFreeCommentId())
                .freeId(comment.getFreeId())
                .userId(comment.getUserId())
                .isDeleted(false)
                .freeComment(commentDto.getComment())
                .pFreeCommentId(comment.getPFreeCommentId())
                .build();
        FreeComment saved = freeCommentRepository.save(comment);
        return toResponse(saved);
    }

    public List<FreeCommentResponse> getComments(Long freeId) {
        FreePost freePost = freePostRepository.findById(freeId)
                .orElseThrow(() -> new RestApiException(FreeErrorStatus.POST_NOT_FOUND));

        return freeCommentRepository.findByFreeId(freePost).stream()
                .map(comment -> FreeCommentResponse.builder()
                        .commentId(comment.getFreeCommentId())
                        .postId(comment.getFreeId())
                        .userId(comment.getUserId())
                        .comment(comment.getIsDeleted() ? "삭제된 댓글입니다." : comment.getFreeComment())
                        .parentCommentId(comment.getPFreeCommentId())
                        .isDeleted(comment.getIsDeleted())
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

        comment = FreeComment.builder()
                .freeCommentId(comment.getFreeCommentId())
                .freeId(comment.getFreeId())
                .userId(comment.getUserId())
                .isDeleted(true)
                .freeComment(comment.getFreeComment())
                .pFreeCommentId(comment.getPFreeCommentId())
                .build();
        freeCommentRepository.save(comment);
    }



    // 결과 응답용
    private FreeCommentResponse toResponse(FreeComment comment) {
        return FreeCommentResponse.builder()
                .commentId(comment.getFreeCommentId())
                .postId(comment.getFreeId())
                .userId(comment.getUserId())
                .comment(comment.getFreeComment())
                .isDeleted(comment.getIsDeleted())
                .parentCommentId(comment.getPFreeCommentId())
                .build();
    }
}
