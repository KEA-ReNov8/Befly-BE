package befly.community.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class AiSummaryResponse {
    private List<EmotionAnalytics> analytics;
    private String totalComment;
    private String suggest;
    private String worry_title;
    private String worry_category;
    private String worry_created_at;
}


