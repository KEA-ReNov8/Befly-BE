package befly.community.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SolvedPostResponse {
    private Long solvedId;
    private Long userId;
    private String solvedTitle;
    private String solvedContent;
    private String imageUrl;
}
