package befly.community.service;

import befly.common.apiPayload.ApiResponse;
import befly.common.exception.RestApiException;
import befly.community.client.ConsultServiceClient;
import befly.community.client.UserServiceClient;
import befly.community.domain.SolvedPost;
import befly.community.dto.AiSummaryResponse;
import befly.community.dto.ListSolvedPostResponse;
import befly.community.dto.SolvedPostRequest;
import befly.community.dto.SolvedPostResponse;
import befly.community.repository.SolvedCommentRepository;
import befly.community.repository.SolvedEmpathyRepository;
import befly.community.repository.SolvedPostRepository;
import befly.community.service.kafka.WingEventProducerService;
import befly.community.status.SolvedErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SolvedPostService {
    private final SolvedPostRepository solvedPostRepository;
    private final WingEventProducerService wingEventProducerService;
    private final ConsultServiceClient consultServiceClient;
    private final UserServiceClient userServiceClient;
    private final SolvedCommentRepository solvedCommentRepository;
    private final SolvedEmpathyRepository solvedEmpathyRepository;

    // 해결함 글 생성
    @Transactional
    public SolvedPostResponse createPost(Long userId, SolvedPostRequest request) {

        SolvedPost post = SolvedPost.builder()
                .userId(userId)
                .solvedTitle(request.getSolvedTitle())
                .solvedContent(request.getSolvedContent())
                .imageKeys(request.getImageKeys())
                .sessionId(request.getSessionId())
                .category(request.getCategory())
                .build();

        SolvedPost saved = solvedPostRepository.save(post);
        wingEventProducerService.produceWingEvent(userId, 10L);

        return null;
    }

    // 해결함 글 수정
    @Transactional
    public SolvedPostResponse updatePost(Long userId, Long id, SolvedPostRequest request) {
        SolvedPost post = solvedPostRepository.findById(id)
                .orElseThrow(() -> new RestApiException(SolvedErrorStatus.POST_NOT_FOUND));
        if (!post.getUserId().equals(userId)) throw new RestApiException(SolvedErrorStatus.NO_PERMISSION);

        post.update(request.getSolvedTitle(), request.getSolvedContent(), request.getImageKeys(), request.getCategory());

        return null;
    }

    // 해결함 글 삭제
    @Transactional
    public void deletePost(Long userId, Long solvedId) {
        SolvedPost post = solvedPostRepository.findById(solvedId)
                .orElseThrow(() -> new RestApiException(SolvedErrorStatus.POST_NOT_FOUND));
        if (!post.getUserId().equals(userId)) {
            throw new RestApiException(SolvedErrorStatus.NO_PERMISSION);
        }
        solvedPostRepository.delete(post);
    }

    // 해결함 글 단건 조회
    @Transactional(readOnly = true)
    public SolvedPostResponse getPost(Long id, Long currentUserId) {
        SolvedPost post = solvedPostRepository.findById(id)
                .orElseThrow(() -> new RestApiException(SolvedErrorStatus.POST_NOT_FOUND));
        AiSummaryResponse aiSummary = consultServiceClient.evaluateChat(post.getSessionId(), currentUserId).getResult();
        String nickname = getNickname(post.getUserId(), currentUserId);
        long commentCount = solvedCommentRepository.countBySolvedId(post);
        long likeCount = solvedEmpathyRepository.countSolvedEmpathyBySolvedId(post.getSolvedId());

        return toResponse(post, nickname, commentCount, likeCount, aiSummary);
    }


    // 최신글 4개 조회
    @Transactional(readOnly = true)
    public List<ListSolvedPostResponse> getLatestPosts(Long currentUserId) {
        List<SolvedPost> posts = solvedPostRepository.findTop4ByOrderByCreatedAtDesc();
        return posts.stream()
                .map(post -> {
                    String nickname = getNickname(post.getUserId(), currentUserId);
                    long commentCount = solvedCommentRepository.countBySolvedId(post);
                    long likeCount = solvedEmpathyRepository.countSolvedEmpathyBySolvedId(post.getSolvedId());

                    List<String> imageUrls = post.getImageKeys() != null
                            ? post.getImageKeys().stream().toList()
                            : List.of();

                    return ListSolvedPostResponse.builder()
                            .solvedId(post.getSolvedId())
                            .nickname(nickname)
                            .solvedTitle(post.getSolvedTitle())
                            .solvedContent(post.getSolvedContent())
                            .imageUrls(imageUrls)
                            .commentCount(commentCount)
                            .likeCount(likeCount)
                            .createdAt(post.getCreatedAt())
                            .updatedAt(post.getUpdatedAt())
                            .category(post.getCategory())
                            .build();
                })
                .collect(Collectors.toList());
    }


    // 페이지네이션 (페이지 사이즈 8, 생성일순)
    @Transactional(readOnly = true)
    public Page<ListSolvedPostResponse> getAllPosts(Long currentUserId, Pageable pageable) {
        return solvedPostRepository.findAll(pageable)
                .map(post -> {
                    String nickname = getNickname(post.getUserId(), currentUserId);
                    long commentCount = solvedCommentRepository.countBySolvedId(post);
                    long likeCount = solvedEmpathyRepository.countSolvedEmpathyBySolvedId(post.getSolvedId());

                    List<String> imageUrls = post.getImageKeys() != null
                            ? post.getImageKeys().stream().toList()
                            : List.of();

                    return ListSolvedPostResponse.builder()
                            .solvedId(post.getSolvedId())
                            .nickname(nickname)
                            .solvedTitle(post.getSolvedTitle())
                            .solvedContent(post.getSolvedContent())
                            .imageUrls(imageUrls)
                            .commentCount(commentCount)
                            .likeCount(likeCount)
                            .createdAt(post.getCreatedAt())
                            .updatedAt(post.getUpdatedAt())
                            .category(post.getCategory())
                            .build();
                });
    }

    //유저 아이디로 해결함 글 조회
    @Transactional(readOnly = true)
    public Page<ListSolvedPostResponse> getPostsByUserId(Long userId, Pageable pageable) {
        return solvedPostRepository.findByUserId(userId, pageable)
                .map(post -> {
                    List<String> imageUrls = post.getImageKeys() != null
                            ? post.getImageKeys()
                            : List.of();

                    return ListSolvedPostResponse.builder()
                            .solvedId(post.getSolvedId())
                            .nickname(userServiceClient.getUserNicknameById(userId).getResult()) // 닉네임 조회
                            .solvedTitle(post.getSolvedTitle())
                            .solvedContent(post.getSolvedContent())
                            .imageUrls(imageUrls)
                            .commentCount(solvedCommentRepository.countBySolvedId(post))
                            .likeCount(solvedEmpathyRepository.countSolvedEmpathyBySolvedId(post.getSolvedId()))
                            .createdAt(post.getCreatedAt())
                            .updatedAt(post.getUpdatedAt())
                            .category(post.getCategory())
                            .build();
                });
    }


    // 닉네임 조회
    private String getNickname(Long targetUserId, Long currentUserId) {
        ApiResponse<String> response = userServiceClient.getUserNicknameById(targetUserId);
        return response.getResult();
    }

    // 응답 변환
    private SolvedPostResponse toResponse(SolvedPost post, String nickname, long commentCount, long likeCount, AiSummaryResponse aiSummary) {
        List<String> imageUrls = post.getImageKeys() != null
                ? post.getImageKeys().stream().toList()
                : List.of();

        return SolvedPostResponse.builder()
                .solvedId(post.getSolvedId())
                .nickname(nickname)
                .solvedTitle(post.getSolvedTitle())
                .solvedContent(post.getSolvedContent())
                .imageUrls(imageUrls)
                .commentCount(commentCount)
                .likeCount(likeCount)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .category(post.getCategory())
                .analytics(aiSummary != null ? aiSummary.getAfterKeyword() : List.of())
                .totalComment(aiSummary != null ? aiSummary.getTotalComment() : null)
                .suggest(aiSummary != null ? aiSummary.getSuggestComment() : null)
                .worryTitle(aiSummary != null ? aiSummary.getWorryTitle() : null)
                .build();
    }



}
