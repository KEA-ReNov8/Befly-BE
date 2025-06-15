package befly.community.dto;

import befly.community.domain.SolvedPost;
import befly.community.domain.comment.SolvedComment;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SolvedCommentResponse {
    private Long commentId;
    private SolvedPost postId;
    private Long userId;
    private String nickname;
    private Long badge;
    private String profileImage;
    private String comment;
    private Boolean isDeleted;
    private SolvedComment parentCommentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}