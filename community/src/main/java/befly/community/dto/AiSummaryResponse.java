package befly.community.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
public class AiSummaryResponse {
    @JsonProperty("after_keyword")
    private List<EmotionAnalytics> afterKeyword;

    @JsonProperty("category")
    private String category;

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("total_comment")
    private String totalComment;

    @JsonProperty("suggest_comment")
    private String suggestComment;

    @JsonProperty("chat_title")
    private String worryTitle;

}

