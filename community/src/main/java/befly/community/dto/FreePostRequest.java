package befly.community.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FreePostRequest {
    private String freeTitle;
    private String freeContent;
    private List<String> imageKeys;
}
