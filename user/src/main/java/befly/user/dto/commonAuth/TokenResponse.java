package befly.user.dto.commonAuth;

import lombok.Getter;
import lombok.Setter;

//SignIn에서 사용할 TokenResponse입니다. refresh랑 access토큰을 넣어서 ApiResponse에 추가할 용도
@Getter
@Setter
public class TokenResponse {
    String accessToken;
    String refreshToken;
}
