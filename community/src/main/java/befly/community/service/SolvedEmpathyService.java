package befly.community.service;

import befly.common.exception.RestApiException;
import befly.community.repository.SolvedEmpathyRepository;
import befly.community.repository.SolvedPostRepository;
import befly.community.domain.empahty.SolvedEmpathy;
import befly.community.dto.kafka.NotificationType;
import befly.community.service.kafka.NotificationProducerService;
import befly.community.status.SolvedErrorStatus;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SolvedEmpathyService {

    private final SolvedEmpathyRepository solvedEmpathyRepository;
    private final NotificationProducerService notificationProducerService;
    private final SolvedPostRepository solvedPostRepository;

    // 해결함 공감 생성
    @Transactional
    public void createEmpathy(Long userId, Long solvedId) {
        if (solvedEmpathyRepository.existsByUserIdAndSolvedId(userId, solvedId)) {
            throw new RestApiException(SolvedErrorStatus.ALREADY_EMPATHIZED);
        }
        SolvedEmpathy empathy = SolvedEmpathy.builder()
                .userId(userId)
                .solvedId(solvedId)
                .build();

        solvedPostRepository.findById(solvedId)
                .ifPresent(solvedEmpathy -> {
                    Long postUserId = solvedEmpathy.getUserId();
                    // 알림을 보내는 조건 (postUserId가 현재 사용자 userId와 다른 경우)도 여기서 처리
                    if (postUserId != null && !postUserId.equals(userId)) { // null 체크 및 본인에게 알림 보내지 않기
                        notificationProducerService.sendNotificationIfNeeded(postUserId, userId, NotificationType.SOLVEDLIKE);
                    }
                });

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
    @Transactional(readOnly = true)
    public boolean isEmpathized(Long userId, Long solvedId) {
        return solvedEmpathyRepository.existsByUserIdAndSolvedId(userId, solvedId);
    }

    // 해결함 글 공감 갯수 확인
    @Transactional(readOnly = true)
    public long countSolvedEmpathy(Long solvedId) {
        return solvedEmpathyRepository.countSolvedEmpathyBySolvedId(solvedId);
    }
}
