package befly.community.service;

import befly.community.dto.FreePostSearchResponse;
import befly.community.dto.SolvedPostSearchResponse;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    /**
     * 해결함 게시글 카테고리/키워드별 8개씩 검색
     */
    public List<FreePostSearchResponse> searchFreePosts(String keyword, int page) {
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

        try {
            SearchResponse<Map> response = elasticsearchClient.search(builder.build(), Map.class);
            ObjectMapper mapper = new ObjectMapper();
            log.info("ES Response: {}", mapper.writeValueAsString(response));
            return response.hits().hits().stream()
                    .map(hit -> convertToFreePostResponse(hit.source()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private FreePostSearchResponse convertToFreePostResponse(Map<String, Object> source) {
        return FreePostSearchResponse.builder()
                .freeId(source.get("free_id") != null ? Long.valueOf(source.get("free_id").toString()) : null)
                .userId(source.get("user_id") != null ? Long.valueOf(source.get("user_id").toString()) : null)
                .freeTitle((String) source.get("free_title"))
                .freeContent((String) source.get("free_content"))
                .imageKeys((List<String>) source.getOrDefault("image_key", Collections.emptyList()))
                .createdAt((String) source.get("created_at"))
                .updatedAt((String) source.get("updated_at"))
                .commentCount(source.get("comment_count") != null ? Long.valueOf(source.get("comment_count").toString()) : 0L)
                .likeCount(source.get("like_count") != null ? Long.valueOf(source.get("like_count").toString()) : 0L)
                .nickname((String) source.get("nickname"))
                .build();
    }
}
