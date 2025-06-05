package befly.community.dto;

import befly.community.domain.FreePost;
import befly.community.domain.comment.FreeComment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FreeCommentResponse {
    private Long commentId;
    private FreePost postId;
    // private Long userId;
    private String nickname;
    private String comment;
    private Boolean isDeleted;
    private FreeComment parentCommentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
