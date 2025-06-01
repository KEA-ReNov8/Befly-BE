package befly.community.repository;

import befly.community.domain.FreePost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FreePostRepository extends JpaRepository<FreePost, Long> {

    List<FreePost> findTop4ByOrderByCreatedAtDesc();
}
