package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.NotificationEntity;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    // ================= DASHBOARD =================
    Page<NotificationEntity> findByUser_Id(Long userId, Pageable pageable);

    // ================= SORTED (BEST PRACTICE) =================
    Page<NotificationEntity> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // ================= OPTIONAL HELPERS =================
    long countByUser_IdAndReadFalse(Long userId);
}