package befly.community.client;

import befly.common.apiPayload.ApiResponse;
import befly.community.dto.AiSummaryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "consult-service", url = "${service.url-consult}")
public interface ConsultServiceClient {

    @GetMapping("/consult/chat/evaluate/result/{session_id}")
    ApiResponse<AiSummaryResponse> evaluateChat(@PathVariable("session_id") String sessionId,
                                                @RequestHeader("X-USER-ID") Long X_USER_ID);
}

