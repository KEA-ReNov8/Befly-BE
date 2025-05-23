package befly.user.dto.commonAuth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequest {
    private String clientId;
    private String password;
    private String nickName;
    private String photoUrl; // 사진 URL 추가
}