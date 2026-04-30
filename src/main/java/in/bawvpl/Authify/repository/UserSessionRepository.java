package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    List<UserSession> findByUserIdAndActiveTrue(Long userId);

    Optional<UserSession> findByIdAndUserId(Long id, Long userId);
}
