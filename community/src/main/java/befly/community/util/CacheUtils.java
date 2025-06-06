package befly.community.util;

import befly.community.client.UserServiceClient;
import befly.community.dto.UserProfileResponse;
import befly.community.dto.UserListResponse;
import java.time.Duration;
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

    public void cacheUserNickName(List<UserProfileResponse> userProfileResponseList) {
        RedisSerializer<String> keySerializer = (RedisSerializer<String>) redisTemplate.getKeySerializer();
        RedisSerializer<UserProfileResponse> valueSerializer = (RedisSerializer<UserProfileResponse>) redisTemplate.getValueSerializer();

        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (UserProfileResponse dto : userProfileResponseList) {
                byte[] keyBytes = keySerializer.serialize("user:nickname:" + dto.getUserId());
                byte[] valueBytes = valueSerializer.serialize(dto);

                connection.set(keyBytes, valueBytes);
                connection.expire(keyBytes, 60 * 60);
            }
            return null;
        });
    }


    public Map<Long, String> getUserNickName(List<Long> userIdList) {
        //유저 리스트에서 키값 추출
        List<String> keys = userIdList.stream()
                .map(id -> "user:nickname:" + id)
                .toList();

        //Redis에 캐싱된 값 load
        List<UserProfileResponse> cachedList = redisTemplate.opsForValue().multiGet(keys);

        //캐싱이 안되어 있는 값들 추출
        Map<Long, String> cachedMap = IntStream.range(0, userIdList.size())
                .boxed()
                .collect(Collectors.toMap(
                        userIdList::get,
                        i -> {
                            UserProfileResponse dto = cachedList.get(i);
                            return dto != null ? dto.getNickName() : null;
                        }
                ));
        List<Long> missedIds = cachedMap.entrySet().stream()
                .filter(e -> e.getValue() == null)
                .map(Map.Entry::getKey)
                .toList();

        //miss된 id가 있는 경우
        if (!missedIds.isEmpty()) {
            //miss 값 요청
            UserListResponse userProfileList = userServiceClient.getUserProfileList(missedIds).getResult();

            // 캐시 저장 + 결과 갱신
            userProfileList.getUsers().forEach(dto -> {
                Long userId = dto.getUserId();
                String nickname = dto.getNickName();

                // 캐시에 저장
                redisTemplate.opsForValue().set("user:nickname:" + userId, dto, Duration.ofMinutes(60));
                cachedMap.put(userId, nickname);
            });
        }

        return cachedMap;
    }
}
