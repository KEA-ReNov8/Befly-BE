package befly.community.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class FreePostResponse {
    private Long freeId;
    // private Long userId;
    private String nickname;
    private Long badge;
    private String freeTitle;
    private String freeContent;
    private List<String> imageUrl;
    private Long likes;
    private Long comments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
