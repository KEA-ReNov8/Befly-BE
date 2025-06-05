package befly.user.dto.commonAuth;

import befly.user.domain.Enum.LoginType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequest {
    private String clientId;
    private String password;
    private String nickName;
    private LoginType loginType;
}