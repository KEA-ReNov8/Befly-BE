package befly.community.domain;

import befly.common.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolvedPost extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long solvedId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 255)
    private String solvedTitle;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String solvedContent;


    @Column(name = "image_key")
    private String imageKey;

    @Column(name = "session_id")
    private String sessionId;

    @Column(nullable = false, length = 20)
    private String category; // 카테고리 필드 추가 ("불안", "상처" 등)

    public void update(String title, String content, String imageKey, String category) {
        if (title != null) this.solvedTitle = title;
        if (content != null) this.solvedContent = content;
        if (imageKey != null) this.imageKey = imageKey;
        if (category != null) this.category = category;
    }
}
