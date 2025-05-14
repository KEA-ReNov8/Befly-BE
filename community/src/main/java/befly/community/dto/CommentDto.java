package befly.community.dto;

import befly.community.dto.kafka.NotificationType;
import lombok.Getter;

@Getter
public class CommentDto {
    Long userId; // 댓글 단 유저의 ID
    Long postId;// 게시글의 ID(PK)
    NotificationType notificationType; //좋아요인지, 해결함인지, 고민함인지 등등 (FREEPOST, LIKE, SOLVEDPOST 받는 걸로 예정)
    Long PcommentId;
    String comment;

}
