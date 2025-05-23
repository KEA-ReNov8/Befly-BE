package befly.community.dto;

import befly.community.domain.SolvedPost;
import befly.community.domain.comment.SolvedComment;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SolvedCommentResponse {
    private Long commentId;
    private SolvedPost postId;
    private Long userId;
    private String comment;
    private Boolean isDeleted;
    private SolvedComment parentCommentId;
}