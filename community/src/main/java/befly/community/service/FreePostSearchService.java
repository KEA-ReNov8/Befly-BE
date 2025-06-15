package befly.community.service;

import befly.common.apiPayload.ApiResponse;
import befly.community.client.UserServiceClient;
import befly.community.domain.comment.FreeComment;
import befly.community.dto.FreePostSearchResponse;
import befly.community.dto.FreePostSearchResult;
import befly.community.dto.SolvedPostSearchResponse;
import befly.community.dto.SolvedPostSearchResult;
import befly.community.dto.UserProfileResponse;
import befly.community.repository.FreePostRepository;
import befly.community.util.CacheUtils;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FreePostSearchService {
    private final ElasticsearchClient elasticsearchClient;
    private final CacheUtils cacheUtils;

    /**
     * 자유함 게시글 키워드별 8개씩 검색
     */
    public FreePostSearchResult searchFreePosts(String keyword, int page) {
        int size = 8; // 한 페이지 8개
        SearchRequest.Builder builder = new SearchRequest.Builder()
                .index("mysql-server_befly_free_post")
                .from(page * size)
                .size(size);

        if (keyword != null && !keyword.isEmpty()) {
            builder.query(q -> q.bool(b -> b
                    .should(sh -> sh.match(m -> m.field("free_title").query(keyword).boost(3.0f)))
                    .should(sh -> sh.match(m -> m.field("free_content").query(keyword).boost(1.0f)))
            ));
        } else {
            builder.query(q -> q.matchAll(m -> m));
        }

        // 검색 실행
        try {
            SearchResponse<JsonData> response = elasticsearchClient.search(builder.build(), JsonData.class);

            long totalElements = response.hits().total().value();
            int totalPages = (int) Math.ceil((double) totalElements / size);

            Map<Long, UserProfileResponse> userProfileResponseMap = cacheUtils.getUserNickName(
                    response.hits().hits().stream()
                            .map(hit -> {
                                Map<String, Object> source = hit.source().to(Map.class);
                                return getLong(source.get("user_id"));
                            })
                            .distinct()
                            .toList()
            );
            List<FreePostSearchResponse> posts = response.hits().hits().stream()
                    .map(hit -> {
                        try {
                            Map<String, Object> source = hit.source().to(Map.class);
                            Long userId = getLong(source.get("user_id"));
                            Long freeId = getLong(source.get("free_id"));

                            if (userId == null || freeId == null) {
                                throw new RuntimeException("user_id 또는 free_id가 null이거나 잘못된 형식입니다.");
                            }

                            String nickname;
                            // 닉네임 조회
                            try {
                                UserProfileResponse profile = userProfileResponseMap.get(userId);
                                nickname = (profile != null && profile.getNickName() != null) ? profile.getNickName() : "익명";
                            } catch (Exception e) {
                                nickname = "익명";
                            }
                            Long badge =  userProfileResponseMap.get(userId).getBadge();

                            return convertToFreePostResponse(source, freeId, nickname, badge);
                        } catch (Exception ex) {
                            throw new RuntimeException("파싱 중 오류 발생", ex);
                        }
                    })
                    .collect(Collectors.toList());

            return FreePostSearchResult.builder()
                    .posts(posts)
                    .currentPage(page)
                    .totalPages(totalPages)
                    .totalElements(totalElements)
                    .hasNext(page < totalPages - 1)
                    .hasPrevious(page > 0)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Elasticsearch 검색 실패", e);
        }
    }

    private FreePostSearchResponse convertToFreePostResponse(Map<String, Object> source, Long freeId, String nickname, Long badge) {
        Object imageKeyRaw = source.get("image_key");
        String imageKey;

        if (imageKeyRaw instanceof List) {
            imageKey = (String) imageKeyRaw;
        } else if (imageKeyRaw instanceof String) {
            imageKey = (String) imageKeyRaw;;
        } else {
            imageKey = null;
        }

        return FreePostSearchResponse.builder()
                .freeId(freeId)
                .userId(getLong(source.get("user_id")))
                .freeTitle((String) source.get("free_title"))
                .freeContent((String) source.get("free_content"))
                .badge(badge)
                .imageKeys(imageKey)
                .createdAt(source.get("created_at") != null ? source.get("created_at").toString() : null)
                .updatedAt(source.get("updated_at") != null ? source.get("updated_at").toString() : null)
                .commentCount(getLong(source.get("comment_count")))
                .likeCount(getLong(source.get("like_count")))
                .nickname(nickname)
                .build();
    }

    private Long getLong(Object obj) {
        if (obj == null) return 0L; // null이면 0L 반환
        try {
            return Long.valueOf(obj.toString());
        } catch (NumberFormatException e) {
            return null; // 변환 실패하면 null 변환
        }
    }

}

