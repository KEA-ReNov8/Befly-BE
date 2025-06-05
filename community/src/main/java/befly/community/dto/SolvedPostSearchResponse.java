package befly.community.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SolvedPostSearchResponse {
    private Long solvedId;
    private Long userId;
    private String solvedTitle;
    private String solvedContent;
    private String category;
    private List<String> imageKeys; // 썸네일/이미지 미리보기
    private String createdAt;
    private String updatedAt;
    private Long commentCount;
    private Long likeCount;
    private String nickname;
}
