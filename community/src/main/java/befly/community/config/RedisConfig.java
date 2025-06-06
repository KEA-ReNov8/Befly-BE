package befly.community.config;

import befly.community.dto.UserProfileResponse;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Profile("prod")
public class RedisConfig {
    @Value("${spring.data.redis.host}") String REDIS_HOST;
    @Value("${spring.data.redis.port}") Integer REDIS_PORT;
    @Value("${spring.data.redis.username}") String REDIS_USERNAME;
    @Value("${spring.data.redis.password}") String REDIS_PASSWORD;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(REDIS_HOST);
        config.setPort(REDIS_PORT);
        config.setUsername(REDIS_USERNAME);
        config.setPassword(REDIS_PASSWORD);

        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, UserProfileResponse> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, UserProfileResponse> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key Serializer
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // ObjectMapper with type info
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // Value Serializer
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }
}
