package befly.community.client;

import befly.common.apiPayload.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service", url = "${service.url}")
public interface UserServiceClient {
    @GetMapping("/user/getNickname/{userId}")
    ApiResponse<String> getUserNicknameById(@PathVariable("userId") Long userId, @RequestHeader("X-USER-ID") Long X_USER_ID);
}
