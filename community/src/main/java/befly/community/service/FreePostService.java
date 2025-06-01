package befly.community.service;

import befly.common.exception.RestApiException;
import befly.common.s3.S3Interface;
import befly.community.dto.LatestFreeResponse;
import befly.community.repository.FreeCommentRepository;
import befly.community.repository.FreeEmpathyRepository;
import befly.community.repository.FreePostRepository;
import befly.community.domain.FreePost;
import befly.community.dto.FreePostRequest;
import befly.community.dto.FreePostResponse;
import befly.community.status.FreeErrorStatus;
import befly.community.util.TimeUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FreePostService {
    private final FreePostRepository freePostRepository;
    private final S3Interface s3Interface;
    private final FreeCommentRepository freeCommentRepository;
    private final FreeEmpathyRepository freeEmpathyRepository;

    // 자유함 글 생성
    @Transactional
    public FreePostResponse createPost(Long userId, FreePostRequest request) {
        FreePost post = FreePost.builder()
                .userId(userId)
                .freeTitle(request.getFreeTitle())
                .freeContent(request.getFreeContent())
                .imageKeys(request.getImageKeys())
                .build();

        log.info("Request imageKeys : {}", request.getImageKeys());
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

    // 자유함 최신 글 조회
    public List<LatestFreeResponse> getLatestFreePosts() {
        // 최근 4개 게시글 조회
        List<FreePost> freePosts = freePostRepository.findTop4ByOrderByCreatedAtDesc();

        // 최종 매핑
        return freePosts.stream()
                .map(freePost -> {
                    Long freeId = freePost.getFreeId(); // 자유함 게시글 아이디
                    Long empathyCount = freeEmpathyRepository.countFreeEmpathyByFreeId(freeId); // 공감 수 조회
                    Long commentCount = freeCommentRepository.countFreeCommentByFreeId(freePost); // 응원 수 조회

                return LatestFreeResponse.builder()
                        .postId(freePost.getFreeId())
                        .title(freePost.getFreeTitle())
                        .content(freePost.getFreeContent())
                        .userId(freePost.getUserId())
                        .likes(empathyCount != null ? empathyCount : 0L)
                        .comments(commentCount != null ? commentCount : 0L)
                        .time(TimeUtils.formatTimeAgo(freePost.getCreatedAt()))
                        .imageUrl(freePost.getImageKeys())
                        .build();
                })
                .toList();

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

        post.updateFreePost(request.getFreeTitle(), request.getFreeContent(), request.getImageKeys());
        // FreePost updated = freePostRepository.save(post);
        return toResponse(post);
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
        List<String> imageUrls = null;

        if (post.getImageKeys() != null && !post.getImageKeys().isEmpty()) {
            imageUrls = post.getImageKeys().stream()
                    .map(s3Interface::getImageUrl)
                    .collect(Collectors.toList());
        }

        return FreePostResponse.builder()
                .freeId(post.getFreeId())
                .userId(post.getUserId())
                .freeTitle(post.getFreeTitle())
                .freeContent(post.getFreeContent())
                .imageUrl(imageUrls)
                .build();
    }
}
