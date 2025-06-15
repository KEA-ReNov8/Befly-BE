package befly.community.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class SolvedPostRequest {
    private String solvedTitle;
    private String solvedContent;
    private String imageKeys;
    private String sessionId; // AI 분석 결과 조회용
    private String category;
}

