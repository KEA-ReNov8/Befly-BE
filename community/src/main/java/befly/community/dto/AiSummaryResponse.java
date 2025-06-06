package befly.community.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
public class AiSummaryResponse {
    private List<EmotionAnalytics> analytics;
    private String totalComment;
    private String suggest;

    @JsonProperty("worry_title")
    private String worryTitle;

    @JsonProperty("worry_category")
    private String worryCategory;

    @JsonProperty("worry_created_at")
    private String worryCreatedAt;
}

