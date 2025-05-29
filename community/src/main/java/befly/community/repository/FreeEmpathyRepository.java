package befly.community.repository;

import befly.community.domain.empahty.FreeEmpathy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FreeEmpathyRepository extends JpaRepository<FreeEmpathy, Long> {

    Optional<FreeEmpathy> findByUserIdAndFreeId(Long userId, Long freeId);

    boolean existsByUserIdAndFreeId(Long userId, Long longId);

    Long countFreeEmpathyByFreeId(Long freeId);
}