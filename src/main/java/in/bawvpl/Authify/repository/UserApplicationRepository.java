package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.UserApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserApplicationRepository extends JpaRepository<UserApplicationEntity, Long> {

    // ✅ FIXED (entityId → id)
    Optional<UserApplicationEntity> findByUser_IdAndApp_AppId(Long userId, Long appId);
}