package befly.community.domain;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection="noti")
@Setter
@Getter
@Builder
public class Notification {
    @Id // MongoDB의 _id 필드에 매핑됩니다.
    private String id;

    private String message; // 저장할 메시지 내용
    private long userId; //알림을 받을 사람의 userId

    @Field("createdAt") // 필드 이름을 명시적으로 지정할 수 있습니다.
    private LocalDateTime createdAt; // 메시지가 생성된 시간
}
