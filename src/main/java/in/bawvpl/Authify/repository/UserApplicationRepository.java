package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.UserApplicationEntity;
import in.bawvpl.Authify.entity.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserApplicationRepository extends JpaRepository<UserApplicationEntity, Long> {

    Optional<UserApplicationEntity> findByUser_IdAndApp_AppId(Long userId, Long appId);

    boolean existsByUser_IdAndApp_AppId(Long userId, Long appId);

    List<UserApplicationEntity> findAllByUser(UserEntity user);

    // ✅ ADD THIS (REQUIRED)
    List<UserApplicationEntity> findAllByUser_Id(Long userId);
}