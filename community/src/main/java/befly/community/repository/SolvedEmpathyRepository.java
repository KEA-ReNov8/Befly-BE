package befly.community.repository;

import befly.community.domain.SolvedPost;
import befly.community.domain.empahty.SolvedEmpathy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SolvedEmpathyRepository extends JpaRepository<SolvedEmpathy, Long> {

    Optional<SolvedEmpathy> findByUserIdAndSolvedId(Long userId, Long solvedId);

    boolean existsByUserIdAndSolvedId(Long userId, Long solvedId);

    long countSolvedEmpathyBySolvedId(Long solvedId);

    void deleteAllBySolvedId(Long solvedId);
}
