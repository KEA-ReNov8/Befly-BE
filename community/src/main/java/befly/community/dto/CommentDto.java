package befly.community.dto;

import befly.community.dto.kafka.NotificationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@NoArgsConstructor
public class CommentDto {
    Long postId;// 게시글의 ID(PK)
    NotificationType notificationType; //좋아요인지, 해결함인지, 고민함인지 등등 (FREEPOST, SOLVEDLIKE, SOLVEDPOST 받는 걸로 예정)
    Long pcommentId;
    String comment;
}
