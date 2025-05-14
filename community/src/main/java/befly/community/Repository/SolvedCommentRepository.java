package befly.community.Repository;

import befly.community.domain.comment.SolvedComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolvedCommentRepository extends JpaRepository<SolvedComment, Long> {
}
