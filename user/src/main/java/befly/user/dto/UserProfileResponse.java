package befly.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private String userName;
    private String nickName;
    private String email;
    private String profileImg;
    private Long wing;
    private Long badge;
}