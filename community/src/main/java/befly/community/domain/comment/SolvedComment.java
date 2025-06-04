package befly.community.domain.comment;


import befly.common.common.BaseTimeEntity;
import befly.community.domain.SolvedPost;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SolvedComment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long solvedCommentId; // solved_comment_id (PK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private SolvedPost solvedId; // 댓글이 달린 글 ID (FK)

    @Column(nullable = false)
    private Long userId; // 댓글 작성자 ID (FK)

    @Column(nullable = false)
    private Boolean isDeleted = false; //댓글 삭제 여부

    @Column(nullable = false, length = 500)
    private String solvedComment; // solved_comment (NOT NULL)

    @ManyToOne
    @JoinColumn(name = "p_solved_comment_id")
    private SolvedComment pSolvedCommentId; // 부모 댓글 ID (대댓글)

}

