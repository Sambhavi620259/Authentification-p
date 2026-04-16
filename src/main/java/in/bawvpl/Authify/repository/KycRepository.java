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
    Optional<KycEntity> findByUser(UserEntity user);

    boolean existsByUser(UserEntity user);

    // ================= SHORTCUT =================
    Optional<KycEntity> findByUser_UserId(String userId);

    // ================= STATUS =================
    List<KycEntity> findByStatusIgnoreCase(String status);

    // ================= ADMIN =================
    long countByStatusIgnoreCase(String status);

    List<KycEntity> findAllByOrderByUploadedAtDesc();
}