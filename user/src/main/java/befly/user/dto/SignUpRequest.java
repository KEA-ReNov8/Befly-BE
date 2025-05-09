package befly.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequest {
    private String userName;
    private String email;
    private String password;
    private String nickName;
    private String photoUrl; // 사진 URL 추가
}