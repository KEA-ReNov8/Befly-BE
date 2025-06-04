package befly.community.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class AiSummaryResponse {
    private List<EmotionAnalytics> emotionCards;
    private String analysisReport;
    private String naraeSuggestion;
}


