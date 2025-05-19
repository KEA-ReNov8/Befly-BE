package befly.community.dto;

import lombok.Getter;

@Getter
public class SolvedPostRequest {
    private String solvedTitle;
    private String solvedContent;
    private String imageKey;
}
