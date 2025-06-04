package befly.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@EnableFeignClients
@EnableJpaAuditing
@SpringBootApplication(scanBasePackages = {"befly.community", "befly.common"})
public class CommunityApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommunityApplication.class, args);
    }
}
