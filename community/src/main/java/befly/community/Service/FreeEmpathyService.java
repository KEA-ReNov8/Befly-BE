package befly.community.Service;

import befly.common.exception.RestApiException;
import befly.community.Repository.FreeEmpathyRepository;
import befly.community.domain.empahty.FreeEmpathy;
import befly.community.status.FreeErrorStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FreeEmpathyService {

    private final FreeEmpathyRepository freeEmpathyRepository;

    @Transactional
    public void createEmpathy(Long userId, Long freeId) {
        boolean exists = freeEmpathyRepository.existsByUserIdAndFreeId(userId, freeId);
        if (exists) {
            throw new RestApiException(FreeErrorStatus.ALREADY_EMPATHIZED);
        }

        FreeEmpathy empathy = FreeEmpathy.builder()
                .userId(userId)
                .freeId(freeId)
                .build();

        freeEmpathyRepository.save(empathy);
    }

    @Transactional
    public void deleteEmpathy(Long userId, Long freeId) {
        FreeEmpathy empathy = freeEmpathyRepository.findByUserIdAndFreeId(userId, freeId)
                .orElseThrow(() -> new RestApiException(FreeErrorStatus.EMPATHY_NOT_FOUND));

        freeEmpathyRepository.delete(empathy);
    }

    public boolean isEmpathized(Long userId, Long freeId) {
        return freeEmpathyRepository.existsByUserIdAndFreeId(userId, freeId);
    }

    public long countFreeEmpathy(Long freeId) {
        return freeEmpathyRepository.countFreeEmpathyByFreeId(freeId);
    }
}