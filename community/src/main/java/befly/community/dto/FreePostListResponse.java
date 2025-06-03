package befly.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class FreePostListResponse {
    private Long postId;
    private String title;
    private String content;
    private Long likes;
    private Long comments;
    private String time;
    // private String nickname; // user 쪽에 API 있음
    private Long userId;
    private List<String> imageUrl;
}
