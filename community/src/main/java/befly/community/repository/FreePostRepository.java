package befly.community.repository;

import befly.community.domain.FreePost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface FreePostRepository extends JpaRepository<FreePost, Long> {

    // 페이징 - 전체 글 리스트 조회
    Page<FreePost> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 페이징 - 특정 유저 글 리스트 조회
    Page<FreePost> findAllByUserId(Long userId, Pageable pageable);

    // 최신 4개 최신순 조회
    List<FreePost> findTop4ByOrderByCreatedAtDesc();

    // 해당 유저가 오늘 글을 작성했는지
    boolean existsByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

}
