package befly.community.domain;

import befly.common.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FreePost extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long freeId; // 자유함 글 id (PK)

    @Column(nullable = false)
    private Long userId; // 글 작성자 id (FK)

    @Column(nullable = false, length = 255)
    private String freeTitle; // 글 제목

    @Column(nullable = false, columnDefinition = "TEXT")
    private String freeContent; // 글 내용

    @Column(name = "image_key")
    private String imageKeys;

    public void updateFreePost(String title, String content, String imageKey) {
        if (title != null) this.freeTitle = title;
        if (content != null) this.freeContent = content;
        if (imageKey != null) this.imageKeys = imageKey;
    }

}
