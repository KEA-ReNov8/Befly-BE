package befly.community.util;

import befly.community.client.UserServiceClient;
import befly.community.dto.UserProfileResponse;
import befly.community.dto.UserListResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CacheUtils {
    private final RedisTemplate<String, UserProfileResponse> redisTemplate;
    private final UserServiceClient userServiceClient;


    public Map<Long, UserProfileResponse> getUserNickName(List<Long> userIdList) {
        RedisSerializer<String> keySerializer = (RedisSerializer<String>) redisTemplate.getKeySerializer();
        RedisSerializer<UserProfileResponse> valueSerializer = (RedisSerializer<UserProfileResponse>) redisTemplate.getValueSerializer();
        //유저 리스트에서 키값 추출
        List<String> keys = userIdList.stream()
                .map(id -> "user:nickname:" + id)
                .toList();

        //Redis에 캐싱된 값 load
        List<UserProfileResponse> cachedList = redisTemplate.opsForValue().multiGet(keys);

        //캐싱이 안되어 있는 값들 추출
        Map<Long, UserProfileResponse> cachedMap = new HashMap<>();
        for (int i = 0; i < userIdList.size(); i++) {
            UserProfileResponse value = (cachedList != null && i < cachedList.size()) ? cachedList.get(i) : null;
            cachedMap.put(userIdList.get(i), value);
        }

        List<Long> missedIds = cachedMap.entrySet().stream()
                .filter(e -> e.getValue() == null)
                .map(Map.Entry::getKey)
                .toList();

        //miss된 id가 있는 경우
        if (!missedIds.isEmpty()) {
            //miss 값 요청
            List<UserProfileResponse> userProfileList = userServiceClient.getUserProfileList(missedIds).getResult().getUsers();

            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                for (UserProfileResponse dto : userProfileList) {
                    byte[] keyBytes = keySerializer.serialize("user:nickname:" + dto.getUserId());
                    byte[] valueBytes = valueSerializer.serialize(dto);

                    connection.set(keyBytes, valueBytes);
                    connection.expire(keyBytes, 60 * 60);
                    cachedMap.put(dto.getUserId(), dto);
                }
                return null;
            });
        }

        return cachedMap;
    }
}
