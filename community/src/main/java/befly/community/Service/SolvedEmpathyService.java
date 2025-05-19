package befly.community.Service;

import befly.common.exception.RestApiException;
import befly.community.Repository.SolvedEmpathyRepository;
import befly.community.domain.empahty.SolvedEmpathy;
import befly.community.status.SolvedErrorStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SolvedEmpathyService {

    private final SolvedEmpathyRepository solvedEmpathyRepository;

    // 해결함 공감 생성
    @Transactional
    public void createEmpathy(Long userId, Long solvedId) {
        boolean exists = solvedEmpathyRepository.existsByUserIdAndSolvedId(userId, solvedId);
        if (exists) {
            throw new RestApiException(SolvedErrorStatus.ALREADY_EMPATHIZED);
        }
        SolvedEmpathy empathy = SolvedEmpathy.builder()
                .userId(userId)
                .solvedId(solvedId)
                .build();
        solvedEmpathyRepository.save(empathy);
    }

    // 해결함 공감 취소
    @Transactional
    public void deleteEmpathy(Long userId, Long solvedId) {
        SolvedEmpathy empathy = solvedEmpathyRepository.findByUserIdAndSolvedId(userId, solvedId)
                .orElseThrow(() -> new RestApiException(SolvedErrorStatus.EMPATHY_NOT_FOUND));
        solvedEmpathyRepository.delete(empathy);
    }

    // 유저가 공감했는지 여부 확인
    public boolean isEmpathized(Long userId, Long solvedId) {
        return solvedEmpathyRepository.existsByUserIdAndSolvedId(userId, solvedId);
    }

    // 해결함 글 공감 갯수 확인
    public long countSolvedEmpathy(Long solvedId) {
        return solvedEmpathyRepository.countSolvedEmpathyBySolvedId(solvedId);
    }
}
