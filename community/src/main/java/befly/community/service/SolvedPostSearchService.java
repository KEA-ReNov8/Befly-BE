package befly.community.service;

import befly.community.client.UserServiceClient;
import befly.community.domain.SolvedPost;
import befly.community.dto.SolvedPostSearchResponse;
import befly.community.dto.SolvedPostSearchResult;
import befly.community.dto.UserProfileResponse;
import befly.community.repository.SolvedPostRepository;
import befly.community.util.CacheUtils;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import befly.common.apiPayload.ApiResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SolvedPostSearchService {
    private final ElasticsearchClient elasticsearchClient;
    private final SolvedPostRepository solvedPostRepository;
    private final CacheUtils cacheUtils;

    /**
     * 해결함 게시글 카테고리/키워드별 8개씩 검색
     */
    public SolvedPostSearchResult searchSolvedPosts(String category, String keyword, int page) {
        int size = 8;
        SearchRequest.Builder builder = new SearchRequest.Builder()
                .index("mysql-server_befly_solved_post")
                .from(page * size)
                .size(size);

        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

        if (category != null && !category.equals("전체")) {
            boolBuilder.filter(f -> f.term(t -> t.field("category").value(category)));
        }

        if (keyword != null && !keyword.isEmpty()) {
            boolBuilder.should(sh -> sh.match(m -> m.field("solved_title").query(keyword).boost(3.0f)))
                    .should(sh -> sh.match(m -> m.field("solved_content").query(keyword).boost(1.0f)))
                    .minimumShouldMatch("1");
        }

        builder.query(q -> q.bool(boolBuilder.build()));

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
            List<SolvedPostSearchResponse> posts = response.hits().hits().stream()
                    .map(hit -> {
                        try {
                            Map<String, Object> source = hit.source().to(Map.class);
                            Long userId = getLong(source.get("user_id"));
                            Long solvedId = getLong(source.get("solved_id"));

                            if (userId == null || solvedId == null) {
                                throw new RuntimeException("user_id 또는 solved_id가 null이거나 잘못된 형식입니다.");
                            }

                            // 닉네임 조회
                            String nickname;
                            Long badge = 0L;
                            try {
                                UserProfileResponse profile = userProfileResponseMap.get(userId);
                                nickname = (profile != null && profile.getNickName() != null) ? profile.getNickName() : "익명";
                                badge = (profile != null && profile.getBadge() != null) ? profile.getBadge() : 0L;
                            } catch (Exception e) {
                                nickname = "익명";
                            }

                            return convertToSolvedPostResponse(source, solvedId, nickname, badge);
                        } catch (Exception ex) {
                            throw new RuntimeException("파싱 중 오류 발생", ex);
                        }
                    })
                    .collect(Collectors.toList());

            return SolvedPostSearchResult.builder()
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

    private SolvedPostSearchResponse convertToSolvedPostResponse(Map<String, Object> source, Long solvedId, String nickname, Long badge) {
        Object imageKeyRaw = source.get("image_key");
        String imageKey;

        if (imageKeyRaw instanceof List) {
            imageKey = (String) imageKeyRaw;
        } else if (imageKeyRaw instanceof String) {
            imageKey = (String) imageKeyRaw;;
        } else {
            imageKey = null;
        }

        String category = solvedPostRepository.findById(solvedId)
                .map(SolvedPost::getCategory)
                .orElse("전체");

        return SolvedPostSearchResponse.builder()
                .solvedId(solvedId)
                .userId(getLong(source.get("user_id")))
                .solvedTitle((String) source.get("solved_title"))
                .solvedContent((String) source.get("solved_content"))
                .category(category)
                .imageKeys(imageKey)
                .createdAt(source.get("created_at") != null ? source.get("created_at").toString() : null)
                .updatedAt(source.get("updated_at") != null ? source.get("updated_at").toString() : null)
                .commentCount(getLong(source.get("comment_count")))
                .likeCount(getLong(source.get("like_count")))
                .badge(badge)
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