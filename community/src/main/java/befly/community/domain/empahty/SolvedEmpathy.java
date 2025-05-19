package befly.community.domain.empahty;

import befly.common.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "solved_empathy")
public class SolvedEmpathy extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "solved_empathy_id")
    private Long solvedEmpathyId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "solved_id", nullable = false)
    private Long solvedId;

}
