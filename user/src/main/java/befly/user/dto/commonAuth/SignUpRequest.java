package befly.user.dto.commonAuth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequest {
    private String userName;
    private String email;
    private String password;
    private String nickname;
    private String photoUrl; // 사진 URL 추가
}