package befly.user.dto.gatewayAuth.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GatewayLoginResponse {
    Boolean signUpStatus;
    String accessToken;
    String refreshToken;
}
