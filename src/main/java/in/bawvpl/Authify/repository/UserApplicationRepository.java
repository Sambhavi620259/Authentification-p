package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.UserApplicationEntity;
import in.bawvpl.Authify.entity.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserApplicationRepository extends JpaRepository<UserApplicationEntity, Long> {

    // ================= SINGLE =================
    Optional<UserApplicationEntity> findByUser_IdAndApp_AppId(Long userId, Long appId);

    // ================= EXISTS (BETTER THAN FETCH) =================
    boolean existsByUser_IdAndApp_AppId(Long userId, Long appId);

    // ================= USER ALL APPS =================
    List<UserApplicationEntity> findAllByUser(UserEntity user);

    // ================= OPTIONAL =================
    List<UserApplicationEntity> findAllByUser_Id(Long userId);
}