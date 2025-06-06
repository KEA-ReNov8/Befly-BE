package befly.community.repository;

import befly.community.domain.SolvedPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SolvedPostRepository extends JpaRepository<SolvedPost, Long> {
    Page<SolvedPost> findAll(Pageable pageable);
    List<SolvedPost> findTop4ByOrderByCreatedAtDesc();
}

