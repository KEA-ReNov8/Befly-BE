package befly.community.service;

import befly.common.exception.RestApiException;
import befly.common.s3.S3Interface;
import befly.community.dto.FreePostListResponse;
import befly.community.dto.NickNameResponse;
import befly.community.repository.FreeCommentRepository;
import befly.community.repository.FreeEmpathyRepository;
import befly.community.repository.FreePostRepository;
import befly.community.domain.FreePost;
import befly.community.dto.FreePostRequest;
import befly.community.dto.FreePostResponse;
import befly.community.service.kafka.WingEventProducerService;
import befly.community.status.FreeErrorStatus;
import befly.community.util.TimeUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FreePostService {
    private final RestTemplate restTemplate;
    private final FreePostRepository freePostRepository;
    private final S3Interface s3Interface;
    private final FreeCommentRepository freeCommentRepository;
    private final FreeEmpathyRepository freeEmpathyRepository;
    private final WingEventProducerService wingEventProducerService;

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

        FreePost saved = freePostRepository.save(post);

        return toResponse(saved, userId);
    }

    // 자유함 글 조회
    public FreePostResponse getPost(Long id, Long userId) {
        FreePost post = freePostRepository.findById(id)
                .orElseThrow(() -> new RestApiException(FreeErrorStatus.POST_NOT_FOUND));
        return toResponse(post, userId);
    }

    // 자유함 글 리스트 조회 (페이지네이션, 페이지 사이즈 8, 생성 시간 순)
    public Page<FreePostListResponse> getAllPosts(Long userId, Pageable pageable) {
        Page<FreePost> freePostsPage = freePostRepository.findAll(pageable);

        return freePostPageMapping(userId, freePostsPage);
    }

    // 자유함 최신 글 조회
    public List<FreePostListResponse> getLatestFreePosts(Long userId) {
        // 최근 4개 게시글 조회
        List<FreePost> freePosts = freePostRepository.findTop4ByOrderByCreatedAtDesc();

        // 최종 매핑 후 반환
        return freePostListMapping(userId, freePosts);
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
        return toResponse(post, userId);
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

    public String getNickName(Long userId, Long targetId) {
        System.out.println("userId: " + userId);
        System.out.println("targetId: " + targetId);
        String api = "http://localhost:8081/user/getNickname/" + targetId;
        System.out.println(api);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-ID", userId.toString());

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<NickNameResponse> response = restTemplate.exchange(
                    api,
                    HttpMethod.GET,
                    entity,
                    NickNameResponse.class
            );

            NickNameResponse body = response.getBody();

            if (body != null && "COMMON200".equals(body.getCode())) {
                return body.getResult();
            } else {
                log.error("Nickname not found for targetId: " + targetId);
                return String.valueOf(targetId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Exception: " + e.getMessage());
            return String.valueOf(targetId);
        }
    }

    public Page<FreePostListResponse> freePostPageMapping(Long userId, Page<FreePost> freePostsPage) {
        return freePostsPage.map(freePost -> {
            Long freeId = freePost.getFreeId();
            Long empathyCount = freeEmpathyRepository.countFreeEmpathyByFreeId(freeId);
            Long commentCount = freeCommentRepository.countFreeCommentByFreeId(freePost);

            List<String> imageUrls = Optional.ofNullable(freePost.getImageKeys())
                    .orElse(List.of())
                    .stream()
                    .map(s3Interface::getImageUrl)
                    .toList();

            return FreePostListResponse.builder()
                    .postId(freeId)
                    .title(freePost.getFreeTitle())
                    .content(freePost.getFreeContent())
                    // .userId(freePost.getUserId())
                    .nickname(getNickName(userId, freePost.getUserId()))
                    .likes(empathyCount != null ? empathyCount : 0L)
                    .comments(commentCount != null ? commentCount : 0L)
                    .time(TimeUtils.formatTimeAgo(freePost.getCreatedAt()))
                    .imageUrl(imageUrls)
                    .build();
        });
    }

    public List<FreePostListResponse> freePostListMapping(Long userId, List<FreePost> freePostList) {
        return freePostList.stream()
                .map(freePost -> {
                    Long freeId = freePost.getFreeId(); // 자유함 게시글 아이디
                    Long empathyCount = freeEmpathyRepository.countFreeEmpathyByFreeId(freeId); // 공감 수 조회
                    Long commentCount = freeCommentRepository.countFreeCommentByFreeId(freePost); // 응원 수 조회

                    // 이미지 키 -> 이미지 URL
                    List<String> imageKeys = freePost.getImageKeys();
                    List<String> imageUrls = imageKeys != null
                            ? imageKeys.stream()
                            .map(s3Interface::getImageUrl)
                            .toList()
                            : List.of();

                    return FreePostListResponse.builder()
                            .postId(freePost.getFreeId())
                            .title(freePost.getFreeTitle())
                            .content(freePost.getFreeContent())
                            // .userId(freePost.getUserId())
                            .nickname(getNickName(userId, freePost.getUserId()))
                            .likes(empathyCount != null ? empathyCount : 0L)
                            .comments(commentCount != null ? commentCount : 0L)
                            .time(TimeUtils.formatTimeAgo(freePost.getCreatedAt()))
                            .imageUrl(imageUrls)
                            .build();
                })
                .toList();
    }


    // 결과 응답용
    private FreePostResponse toResponse(FreePost post, Long userId) {
        List<String> imageUrls = null;

        Long empathyCount = freeEmpathyRepository.countFreeEmpathyByFreeId(post.getFreeId()); // 공감 수 조회
        Long commentCount = freeCommentRepository.countFreeCommentByFreeId(post); // 응원 수 조회

        if (post.getImageKeys() != null && !post.getImageKeys().isEmpty()) {
            imageUrls = post.getImageKeys().stream()
                    .map(s3Interface::getImageUrl)
                    .collect(Collectors.toList());
        }

        return FreePostResponse.builder()
                .freeId(post.getFreeId())
                // .userId(post.getUserId())
                .nickname(getNickName(userId, post.getUserId()))
                .freeTitle(post.getFreeTitle())
                .freeContent(post.getFreeContent())
                .imageUrl(imageUrls)
                .likes(empathyCount)
                .comments(commentCount)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
