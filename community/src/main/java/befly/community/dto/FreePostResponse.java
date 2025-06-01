package befly.community.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class FreePostResponse {
    private Long freeId;
    private Long userId;
    private String freeTitle;
    private String freeContent;
    private List<String> imageUrl;
}
