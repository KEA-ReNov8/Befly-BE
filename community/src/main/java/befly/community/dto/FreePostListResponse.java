package befly.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
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
    private LocalDateTime createdAt;
    private String nickname;
    private Long badge;
    private String imageUrl;
}
