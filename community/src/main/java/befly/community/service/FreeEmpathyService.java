package befly.community.service;

import befly.common.exception.RestApiException;
import befly.community.repository.FreeEmpathyRepository;
import befly.community.repository.FreePostRepository;
import befly.community.domain.empahty.FreeEmpathy;
import befly.community.dto.kafka.NotificationType;
import befly.community.service.kafka.NotificationProducerService;
import befly.community.status.FreeErrorStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FreeEmpathyService {

    private final FreeEmpathyRepository freeEmpathyRepository;
    private final NotificationProducerService notificationProducerService;
    private final FreePostRepository freePostRepository;

    // 공감 생성
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

        freePostRepository.findById(freeId)
                .ifPresent(freePost -> {
                    Long postUserId = freePost.getUserId();
                    // 알림을 보내는 조건 (postUserId가 현재 사용자 userId와 다른 경우)도 여기서 처리
                    if (postUserId != null && !postUserId.equals(userId)) { // null 체크 및 본인에게 알림 보내지 않기
                        notificationProducerService.sendNotificationIfNeeded(postUserId, userId, NotificationType.FREELIKE);
                    }
                });

        freeEmpathyRepository.save(empathy);
    }

    // 공감 삭제
    @Transactional
    public void deleteEmpathy(Long userId, Long freeId) {
        FreeEmpathy empathy = freeEmpathyRepository.findByUserIdAndFreeId(userId, freeId)
                .orElseThrow(() -> new RestApiException(FreeErrorStatus.EMPATHY_NOT_FOUND));

        freeEmpathyRepository.delete(empathy);
    }

    // 공감 여부
    public boolean isEmpathized(Long userId, Long freeId) {
        return freeEmpathyRepository.existsByUserIdAndFreeId(userId, freeId);
    }

}