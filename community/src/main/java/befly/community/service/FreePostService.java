package befly.community.service;

import befly.common.exception.RestApiException;
import befly.common.s3.S3Interface;
import befly.community.repository.FreePostRepository;
import befly.community.domain.FreePost;
import befly.community.dto.FreePostRequest;
import befly.community.dto.FreePostResponse;
import befly.community.status.FreeErrorStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FreePostService {
    private final FreePostRepository freePostRepository;
    private final S3Interface s3Interface;

    // 자유함 글 생성
    @Transactional
    public FreePostResponse createPost(Long userId, FreePostRequest request) {
        FreePost post = FreePost.builder()
                .userId(userId)
                .freeTitle(request.getFreeTitle())
                .freeContent(request.getFreeContent())
                .imageKey(request.getImageKey())
                .build();

        FreePost saved = freePostRepository.save(post);
        return toResponse(saved);
    }

    // 자유함 글 조회
    public FreePostResponse getPost(Long id) {
        FreePost post = freePostRepository.findById(id)
                .orElseThrow(() -> new RestApiException(FreeErrorStatus.POST_NOT_FOUND));
        return toResponse(post);
    }

    // 자유함 글 리스트 조회
    public List<FreePostResponse> getAllPosts() {
        return freePostRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // 자유함 글 수정
    @Transactional
    public FreePostResponse updatePost(Long userId, Long id, FreePostRequest request) {
        FreePost post = freePostRepository.findById(id)
                .orElseThrow(() -> new RestApiException(FreeErrorStatus.POST_NOT_FOUND));

        // 작성자 본인 확인
        if (!post.getUserId().equals(userId)) {
            throw new RestApiException(FreeErrorStatus.NO_PERMISSION);
        }

        FreePost updated = FreePost.builder()
                .freeId(post.getFreeId())
                .userId(userId)
                .freeTitle(request.getFreeTitle())
                .freeContent(request.getFreeContent())
                .imageKey(request.getImageKey())
                .build();

        return toResponse(freePostRepository.save(updated));
    }

    // 자유함 글 삭제
    @Transactional
    public void deletePost(Long userId, Long id) {
        FreePost post = freePostRepository.findById(id)
                .orElseThrow(() -> new RestApiException(FreeErrorStatus.POST_NOT_FOUND));

        // 작성자 본인 확인
        if (!post.getUserId().equals(userId)) {
            throw new RestApiException(FreeErrorStatus.NO_PERMISSION);
        }

        freePostRepository.delete(post);
    }


    // 결과 응답용
    private FreePostResponse toResponse(FreePost post) {
        return FreePostResponse.builder()
                .freeId(post.getFreeId())
                .userId(post.getUserId())
                .freeTitle(post.getFreeTitle())
                .freeContent(post.getFreeContent())
                .imageUrl(post.getImageKey() != null ? s3Interface.getImageUrl(post.getImageKey()) : null)
                .build();
    }
}
