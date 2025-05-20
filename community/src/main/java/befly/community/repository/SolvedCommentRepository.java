package befly.community.repository;

import befly.community.domain.SolvedPost;
import befly.community.domain.comment.SolvedComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolvedCommentRepository extends JpaRepository<SolvedComment, Long> {
    List<SolvedComment> findBySolvedId(SolvedPost solvedId);
}
