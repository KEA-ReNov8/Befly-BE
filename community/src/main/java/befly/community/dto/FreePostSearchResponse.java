package befly.community.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FreePostSearchResponse {
    private Long freeId;
    private Long userId;
    private String freeTitle;
    private String freeContent;
    private String imageKey; // 썸네일/이미지 미리보기
    private String createdAt;
    private String updatedAt;
    private Long commentCount;
    private Long likeCount;
    private String nickname;
    private Long badge;
}
