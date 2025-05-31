package befly.user.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

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
}
