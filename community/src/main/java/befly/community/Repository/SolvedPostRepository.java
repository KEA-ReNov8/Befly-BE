package befly.community.Repository;

import befly.community.domain.SolvedPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolvedPostRepository extends JpaRepository<SolvedPost, Long> {
}
