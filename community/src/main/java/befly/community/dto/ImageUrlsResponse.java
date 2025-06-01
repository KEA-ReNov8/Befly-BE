package befly.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ImageUrlsResponse {
    private String imageKey;
    private String getUrl; // 다운로드용
    private String putUrl; // 업로드용
    private String imageUrl; // 조회용

}
