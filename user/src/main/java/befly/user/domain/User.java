package befly.user.domain;

import befly.common.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 자동 생성 전략
    private Long userId; // user_id (PK)

    @Column(nullable = false, length = 255)
    private String userName; // user_name (NOT NULL)

    @Column(nullable = false, unique = true, length = 255)
    private String clientId; // email (NOT NULL, UNIQUE) -> 로그인용 아이디 및 카카오 아이디

    @Column(length = 255)
    private String profileImg; // profile_img

    @Column
    private Long wing; // wing

    @Column
    private Long badge; // badge

    @Column
    private String password;

    @Column(unique = true, length = 255)
    private String nickName;


    public void updateNickName(String newNickName) {
        this.nickName = newNickName;
    }
}