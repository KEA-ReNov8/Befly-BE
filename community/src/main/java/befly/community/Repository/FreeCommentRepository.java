package befly.community.Repository;

import befly.community.domain.FreePost;
import befly.community.domain.comment.FreeComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FreeCommentRepository extends JpaRepository<FreeComment, Long> {
    List<FreeComment> findByFreeId(FreePost freeId);
   }