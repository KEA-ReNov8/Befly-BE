package befly.community.dto;

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
    private String nickname;
    private String solvedTitle;
    private String solvedContent;
    private List<String> imageUrls;
    private Long commentCount;
    private Long likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String category;
    // AI 분석 결과
    private List<EmotionAnalytics> analytics;
    private String totalComment;
    private String suggest;
    private String worry_title;
    private String worry_category;
    private String worry_created_at;
}

