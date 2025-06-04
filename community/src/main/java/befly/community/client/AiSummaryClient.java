package befly.community.client;

import befly.community.dto.AiSummaryResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AiSummaryClient {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String aiServerUrl = "https://mmgmbjdywjxazdxx.tunnel.elice.io/consult/chat/evaluate/";


    public AiSummaryResponse getSummary(String sessionId) {
        return restTemplate.getForObject(aiServerUrl + sessionId, AiSummaryResponse.class);
    }
}
