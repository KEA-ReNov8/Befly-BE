package befly.community.service;

import befly.common.exception.RestApiException;
import befly.common.s3.S3Interface;
import befly.community.repository.SolvedPostRepository;
import befly.community.domain.SolvedPost;
import befly.community.dto.SolvedPostRequest;
import befly.community.dto.SolvedPostResponse;
import befly.community.service.kafka.WingEventProducerService;
import befly.community.status.SolvedErrorStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SolvedPostService {
    private final SolvedPostRepository solvedPostRepository;
    private final S3Interface s3Interface;
    private final WingEventProducerService wingEventProducerService;

    // 해결함 글 생성
    @Transactional
    public SolvedPostResponse createPost(Long userId, SolvedPostRequest request) {
        SolvedPost post = SolvedPost.builder()
                .userId(userId)
                .solvedTitle(request.getSolvedTitle())
                .solvedContent(request.getSolvedContent())
                .imageKey(request.getImageKey())
                .build();

        SolvedPost saved = solvedPostRepository.save(post);

        // SolvedPost는 하루 제한 없이, 생성 시 10 wings
        wingEventProducerService.produceWingEvent(userId, 10L);

        return toResponse(saved);
    }

    // 해결함 글 수정
    @Transactional
    public SolvedPostResponse updatePost(Long userId, Long id, SolvedPostRequest request) {
        SolvedPost post = solvedPostRepository.findById(id)
                .orElseThrow(() -> new RestApiException(SolvedErrorStatus.POST_NOT_FOUND));

        // 작성자 본인 확인
        if (!post.getUserId().equals(userId)) {
            throw new RestApiException(SolvedErrorStatus.NO_PERMISSION);
        }

        SolvedPost updated = SolvedPost.builder()
                .solvedId(post.getSolvedId())
                .userId(userId)
                .solvedTitle(request.getSolvedTitle())
                .solvedContent(request.getSolvedContent())
                .imageKey(request.getImageKey())
                .build();

        return toResponse(solvedPostRepository.save(updated));
    }

    // 해결함 글 조회
    public SolvedPostResponse getPost(Long id) {
        SolvedPost post = solvedPostRepository.findById(id)
                .orElseThrow(() -> new RestApiException(SolvedErrorStatus.POST_NOT_FOUND));
        return toResponse(post);
    }

    // 해결함 글 리스트 조회
    public List<SolvedPostResponse> getAllPosts() {
        return solvedPostRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // 해결함 글 삭제
    @Transactional
    public void deletePost(Long userId, Long id) {
        SolvedPost post = solvedPostRepository.findById(id)
                .orElseThrow(() -> new RestApiException(SolvedErrorStatus.POST_NOT_FOUND));

        // 작성자 본인 확인
        if (!post.getUserId().equals(userId)) {
            throw new RestApiException(SolvedErrorStatus.NO_PERMISSION);
        }

        solvedPostRepository.delete(post);
    }

    // 결과 응답용
    private SolvedPostResponse toResponse(SolvedPost post) {
        return SolvedPostResponse.builder()
                .solvedId(post.getSolvedId())
                .userId(post.getUserId())
                .solvedTitle(post.getSolvedTitle())
                .solvedContent(post.getSolvedContent())
                .imageUrl(post.getImageKey() != null ? s3Interface.getImageUrl(post.getImageKey()) : null)
                .build();
    }
}