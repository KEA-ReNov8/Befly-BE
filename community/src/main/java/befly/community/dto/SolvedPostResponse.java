package befly.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class SolvedPostResponse {
    private Long solvedId;
    private Long userId;
    private String nickname;
    private Long badge;
    private String solvedTitle;
    private String solvedContent;
    private String imageUrls;
    private Long commentCount;
    private Long likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String category;
    // AI 분석 결과
    private List<EmotionAnalytics> analytics;
    private String totalComment;
    private String suggest;
    private String worryTitle;
}

