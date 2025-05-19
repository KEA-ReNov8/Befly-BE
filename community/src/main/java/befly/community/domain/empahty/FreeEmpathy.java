package befly.community.domain.empahty;

import befly.common.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "free_empathy")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FreeEmpathy extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long freeEmpathyId; // 자유함 글 좋아요 ID (PK)

    @Column(name = "user_id", nullable = false)
    private Long userId; // 좋아요 누른 유저 (FK)

    @Column(name = "free_id", nullable = false)
    private Long freeId; // 좋아요 누른 글 (FK)
}
