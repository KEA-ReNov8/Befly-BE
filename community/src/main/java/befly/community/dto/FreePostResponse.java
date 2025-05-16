package befly.community.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FreePostResponse {
    private Long freeId;
    private Long userId;
    private String freeTitle;
    private String freeContent;
    private String imageUrl;
}
