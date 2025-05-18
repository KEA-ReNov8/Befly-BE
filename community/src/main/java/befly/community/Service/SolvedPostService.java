package befly.community.Service;

import befly.common.exception.RestApiException;
import befly.common.s3.S3Interface;
import befly.community.Repository.SolvedPostRepository;
import befly.community.domain.SolvedPost;
import befly.community.dto.SolvedPostRequest;
import befly.community.dto.SolvedPostResponse;
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
        return toResponse(saved);
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