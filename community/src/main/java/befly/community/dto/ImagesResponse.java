package befly.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ImagesResponse {
    private String imageKey;
    private String uploadUrl;
    private String imageUrl;
}
