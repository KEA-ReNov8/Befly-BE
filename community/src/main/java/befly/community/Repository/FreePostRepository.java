package befly.community.Repository;

import befly.community.domain.FreePost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FreePostRepository extends JpaRepository<FreePost, Long> {
}
