package befly.user.dto.commonAuth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignInRequest {
    private String clientId;
    private String password;
}
