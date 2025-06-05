package befly.community.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmotionAnalytics {
    private String emotion;
    private double score;
    private String comment;
}
