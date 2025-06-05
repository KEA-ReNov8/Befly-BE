package befly.community.service;

import befly.community.dto.SolvedPostSearchResponse;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SolvedPostSearchService {
    private final ElasticsearchClient elasticsearchClient;

    /**
     * 해결함 게시글 카테고리/키워드별 8개씩 검색
     */
    public List<SolvedPostSearchResponse> searchSolvedPosts(String category, String keyword, int page) {
        int size = 8; // 한 페이지 8개
        SearchRequest.Builder builder = new SearchRequest.Builder()
                .index("mysql-server_befly_solved_post")
                .from(page * size)
                .size(size);

        // 카테고리/키워드 검색 쿼리
        if (category != null && !category.equals("전체")) {
            builder.query(q -> q.bool(b -> b
                    .must(m -> m.term(t -> t.field("category").value(category)))
                    .should(sh -> sh.match(m -> m.field("solved_title").query(keyword).boost(3.0f)))
                    .should(sh -> sh.match(m -> m.field("solved_content").query(keyword).boost(1.0f)))
            ));
        } else {
            if (keyword != null && !keyword.isEmpty()) {
                builder.query(q -> q.bool(b -> b
                        .should(sh -> sh.match(m -> m.field("solved_title").query(keyword).boost(3.0f)))
                        .should(sh -> sh.match(m -> m.field("solved_content").query(keyword).boost(1.0f)))
                ));
            } else {
                builder.query(q -> q.matchAll(m -> m));
            }
        }

        try {
            SearchResponse<Map> response = elasticsearchClient.search(builder.build(), Map.class);
            return response.hits().hits().stream()
                    .map(hit -> convertToSolvedPostResponse(hit.source()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private SolvedPostSearchResponse convertToSolvedPostResponse(Map<String, Object> source) {
        return SolvedPostSearchResponse.builder()
                .solvedId(source.get("solved_id") != null ? Long.valueOf(source.get("solved_id").toString()) : null)
                .userId(source.get("user_id") != null ? Long.valueOf(source.get("user_id").toString()) : null)
                .solvedTitle((String) source.get("solved_title"))
                .solvedContent((String) source.get("solved_content"))
                .category((String) source.get("category"))
                .imageKeys((List<String>) source.getOrDefault("image_key", Collections.emptyList()))
                .createdAt((String) source.get("created_at"))
                .updatedAt((String) source.get("updated_at"))
                .commentCount(source.get("comment_count") != null ? Long.valueOf(source.get("comment_count").toString()) : 0L)
                .likeCount(source.get("like_count") != null ? Long.valueOf(source.get("like_count").toString()) : 0L)
                .nickname((String) source.get("nickname"))
                .build();
    }
}
