package befly.community.client;

import befly.common.apiPayload.ApiResponse;
import befly.community.dto.UserListResponse;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service", url = "${service.url}")
public interface UserServiceClient {
    @GetMapping("/user/getNickname/{userId}")
    ApiResponse<String> getUserNicknameById(@PathVariable("userId") Long userId);

    @GetMapping("/user/profiles")
    ApiResponse<UserListResponse> getUserProfileList(@RequestParam List<Long> userIds);
}
