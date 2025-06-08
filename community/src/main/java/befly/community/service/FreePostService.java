package befly.community.service;

import befly.common.exception.RestApiException;
import befly.community.dto.FreePostListResponse;
import befly.community.dto.UserProfileResponse;
import befly.community.repository.FreeCommentRepository;
import befly.community.repository.FreeEmpathyRepository;
import befly.community.repository.FreePostRepository;
import befly.community.domain.FreePost;
import befly.community.dto.FreePostRequest;
import befly.community.dto.FreePostResponse;
import befly.community.service.kafka.WingEventProducerService;
import befly.community.status.FreeErrorStatus;
import befly.community.util.CacheUtils;
import befly.community.util.TimeUtils;
import jakarta.transaction.Transactional;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FreePostService {
    private final FreePostRepository freePostRepository;
    private final FreeCommentRepository freeCommentRepository;
    private final FreeEmpathyRepository freeEmpathyRepository;
    private final WingEventProducerService wingEventProducerService;
    private final CacheUtils cacheUtils;

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

        // FreePost는 하루 한 번, 5 wings
        // 작성 시점 기준 하루의 시작과 끝
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        log.info("Start today : {}", todayStart);
        log.info("End today : {}", todayEnd);

        // 하루의 시작과 끝 기준으로 데이터 조회
        boolean alreadyPostedToday = freePostRepository.existsByUserIdAndCreatedAtBetween(userId, todayStart, todayEnd);
        log.info("[FreePostService] AlreadyPostedToday : {}", alreadyPostedToday);

        // 데이터 존재하지 않아야 발급
        if (!alreadyPostedToday) {
            log.info("[FreePostService] wing event 실행 시작");
            wingEventProducerService.produceWingEvent(userId, 5L);
            log.info("[FreePostService] wing event 실행 완료");
        }

        freePostRepository.save(post);

        return null;
    }

    // 자유함 글 조회
    public FreePostResponse getPost(Long id) {
        FreePost post = freePostRepository.findById(id)
                .orElseThrow(() -> new RestApiException(FreeErrorStatus.POST_NOT_FOUND));
        Map<Long, UserProfileResponse> userProfileResponseMap = cacheUtils.getUserNickName(
                List.of(post.getUserId())
        );
        return toResponse(post, userProfileResponseMap.get(post.getUserId()));
    }

    // 유저 아이디로 글 조회
    public List<FreePostListResponse> getPostByUserId(Long userId) {
        List<FreePost> freePostsList = freePostRepository.findAllByUserIdOrderByCreatedAtDesc(userId);

        return freePostListMapping(freePostsList);
    }

    // 자유함 글 리스트 조회 (페이지네이션, 페이지 사이즈 8, 생성 시간 순)
    public Page<FreePostListResponse> getAllPosts(Pageable pageable) {
        Page<FreePost> freePostsPage = freePostRepository.findAllByOrderByCreatedAtDesc(pageable);

        return freePostPageMapping(freePostsPage);
    }

    // 자유함 최신 글 조회
    public List<FreePostListResponse> getLatestFreePosts() {
        // 최근 4개 게시글 조회
        List<FreePost> freePosts = freePostRepository.findTop4ByOrderByCreatedAtDesc();

        // 최종 매핑 후 반환
        return freePostListMapping(freePosts);
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
        return null;
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
        freeCommentRepository.deleteAllByFreeId(post);
        freeEmpathyRepository.deleteAllByFreeId(id);
        freePostRepository.delete(post);
    }

    // 페이징 응답 매핑 - 전체
    public Page<FreePostListResponse> freePostPageMapping(Page<FreePost> freePostsPage) {
        Map<Long, UserProfileResponse> userProfileResponseMap = cacheUtils.getUserNickName(
                freePostsPage.getContent().stream()
                        .map(FreePost::getUserId)
                        .distinct()
                        .toList()
        );
        return freePostsPage.map(it->mapToListResponse(it, userProfileResponseMap.get(it.getUserId())));
    }

    // 리스트 응답 매핑 - 최신글, 유저
    public List<FreePostListResponse> freePostListMapping(List<FreePost> freePostList) {
        Map<Long, UserProfileResponse> userProfileResponseMap = cacheUtils.getUserNickName(
                freePostList.stream()
                        .map(FreePost::getUserId)
                        .distinct()
                        .toList()
        );
        return freePostList.stream()
                .map(it-> mapToListResponse(it, userProfileResponseMap.get(it.getUserId())))
                .toList();
    }

    // FreePost 엔티티 -> FreePostListResponse 변환
    private FreePostListResponse mapToListResponse(FreePost freePost, UserProfileResponse userProfileResponse) {
        Long empathyCount = freeEmpathyRepository.countFreeEmpathyByFreeId(freePost.getFreeId());
        Long commentCount = freeCommentRepository.countFreeCommentByFreeId(freePost);

        List<String> imageUrls = Optional.ofNullable(freePost.getImageKeys()).orElse(List.of());

        return FreePostListResponse.builder()
                .postId(freePost.getFreeId())
                .title(freePost.getFreeTitle())
                .content(freePost.getFreeContent())
                .badge(userProfileResponse.getBadge())
                .nickname(userProfileResponse.getNickName())
                .likes(empathyCount != null ? empathyCount : 0L)
                .comments(commentCount != null ? commentCount : 0L)
                .time(TimeUtils.formatTimeAgo(freePost.getCreatedAt()))
                .createdAt(freePost.getCreatedAt())
                .imageUrl(imageUrls)
                .build();
    }

    // 결과 응답용 (기본 작성, 조회, 수정)
    private FreePostResponse toResponse(FreePost post, UserProfileResponse userProfileResponse) {
        Long empathyCount = freeEmpathyRepository.countFreeEmpathyByFreeId(post.getFreeId()); // 공감 수 조회
        Long commentCount = freeCommentRepository.countFreeCommentByFreeId(post); // 응원 수 조회

        List<String> imageUrls = Optional.ofNullable(post.getImageKeys()).orElse(List.of());

        return FreePostResponse.builder()
                .freeId(post.getFreeId())
                .badge(userProfileResponse.getUserId())
                .badge(userProfileResponse.getBadge())
                .nickname(userProfileResponse.getNickName())
                .freeTitle(post.getFreeTitle())
                .freeContent(post.getFreeContent())
                .imageUrl(imageUrls)
                .likes(empathyCount != null ? empathyCount : 0L)
                .comments(commentCount != null ? commentCount : 0L)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
