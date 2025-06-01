package befly.community.repository;

import befly.community.domain.FreePost;
import befly.community.domain.comment.FreeComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface FreeCommentRepository extends JpaRepository<FreeComment, Long> {
    List<FreeComment> findByFreeId(FreePost freeId);

    Long countFreeCommentByFreeId(FreePost freeId);

}