package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    // ================= GET ACTIVE SESSIONS =================
    List<UserSession> findByUserIdAndActiveTrue(Long userId);

    // ================= GET SPECIFIC SESSION =================
    Optional<UserSession> findByIdAndUserId(Long id, Long userId);

    // ================= FIND BY TOKEN (IMPORTANT) =================
    Optional<UserSession> findByTokenAndActiveTrue(String token);

    // ================= LOGOUT ALL =================
    List<UserSession> findByUserId(Long userId);

    // ================= BULK DEACTIVATE =================
    void deleteByUserId(Long userId); // optional cleanup

}