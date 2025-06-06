package befly.community.service;

import befly.community.client.UserServiceClient;
import befly.community.domain.SolvedPost;
import befly.community.dto.SolvedPostSearchResponse;
import befly.community.repository.SolvedPostRepository;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
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
    private final UserServiceClient userServiceClient;
    private final SolvedPostRepository solvedPostRepository;

    /**
     * 해결함 게시글 카테고리/키워드별 8개씩 검색
     */
    public List<SolvedPostSearchResponse> searchSolvedPosts(String category, String keyword, int page) {
        int size = 8;
        SearchRequest.Builder builder = new SearchRequest.Builder()
                .index("mysql-server_befly_solved_post")
                .from(page * size)
                .size(size);

        // 쿼리 조건 설정
        if (category != null && !category.equals("전체")) {
            builder.query(q -> q.bool(b -> b
                    .must(m -> m.term(t -> t.field("category").value(category)))
                    .should(sh -> sh.match(m -> m.field("solved_title").query(keyword).boost(3.0f)))
                    .should(sh -> sh.match(m -> m.field("solved_content").query(keyword).boost(1.0f)))
            ));
        } else if (keyword != null && !keyword.isEmpty()) {
            builder.query(q -> q.bool(b -> b
                    .should(sh -> sh.match(m -> m.field("solved_title").query(keyword).boost(3.0f)))
                    .should(sh -> sh.match(m -> m.field("solved_content").query(keyword).boost(1.0f)))
            ));
        } else {
            builder.query(q -> q.matchAll(m -> m));
        }

        // 검색 실행
        try {
            SearchResponse<JsonData> response = elasticsearchClient.search(builder.build(), JsonData.class);

            return response.hits().hits().stream()
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
                            try {
                                ApiResponse<String> nicknameResponse = userServiceClient.getUserNicknameById(userId);
                                nickname = (nicknameResponse != null) ? nicknameResponse.getResult() : "익명";
                            } catch (Exception e) {
                                nickname = "익명";
                            }

                            return convertToSolvedPostResponse(source, solvedId, nickname);
                        } catch (Exception ex) {
                            throw new RuntimeException("파싱 중 오류 발생", ex);
                        }
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Elasticsearch 검색 실패", e);
        }
    }

    private SolvedPostSearchResponse convertToSolvedPostResponse(Map<String, Object> source, Long solvedId, String nickname) {
        Object imageKeyRaw = source.get("image_key");
        List<String> imageKeys;

        if (imageKeyRaw instanceof List) {
            imageKeys = (List<String>) imageKeyRaw;
        } else if (imageKeyRaw instanceof String) {
            imageKeys = List.of((String) imageKeyRaw);
        } else {
            imageKeys = Collections.emptyList();
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
                .imageKeys(imageKeys)
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