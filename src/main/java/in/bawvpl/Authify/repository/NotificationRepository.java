package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.NotificationEntity;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    // ✅ Sorted notifications (USED in dashboard)
    Page<NotificationEntity> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // ✅ Count unread notifications
    long countByUser_IdAndReadFalse(Long userId);
}