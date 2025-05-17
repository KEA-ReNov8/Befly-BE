package befly.user.repository.userRepository;

import befly.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Boolean existsByNickName(String nickName);
    Boolean existsByClientId(String clientId);
    Optional<User> findByClientId(String clientId);
}
