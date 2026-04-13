package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.KycEntity;
import in.bawvpl.Authify.entity.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface KycRepository extends JpaRepository<KycEntity, Long> {

    // ================= BASIC =================

    // Get KYC by User (MOST IMPORTANT)
    Optional<KycEntity> findByUser(UserEntity user);

    // Check if KYC exists
    boolean existsByUser(UserEntity user);

    // ================= ADVANCED =================

    // Get KYC by userId (shortcut)
    Optional<KycEntity> findByUser_UserId(String userId);

    // Get all KYC by status (PENDING / VERIFIED / REJECTED)
    List<KycEntity> findByStatus(String status);

    // ================= OPTIONAL (RECOMMENDED) =================

    // Get all KYC for a specific user (safe fallback)
    List<KycEntity> findAllByUser(UserEntity user);
}