package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.KycEntity;
import in.bawvpl.Authify.entity.UserEntity;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface KycRepository extends JpaRepository<KycEntity, Long> {

    // ================= BASIC =================
    Optional<KycEntity> findByUser(UserEntity user);

    boolean existsByUser(UserEntity user);

    // ================= SHORTCUT =================
    Optional<KycEntity> findByUser_UserId(String userId);

    // ================= STATUS =================
    List<KycEntity> findByStatusIgnoreCase(String status);

    long countByStatusIgnoreCase(String status);

    List<KycEntity> findAllByOrderByUploadedAtDesc();

    // ================= DASHBOARD (IMPORTANT) =================
    @Query("SELECT k.status FROM KycEntity k WHERE k.user.id = :userId")
    String findStatusByUser_Id(@Param("userId") Long userId);
}