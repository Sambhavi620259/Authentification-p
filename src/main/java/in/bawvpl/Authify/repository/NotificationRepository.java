package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.NotificationEntity;
import in.bawvpl.Authify.entity.UserEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    // ================= LIST =================
    List<NotificationEntity> findByUserOrderByCreatedAtDesc(UserEntity user);

    // ================= PAGINATION (REQUIRED) =================
    Page<NotificationEntity> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // ================= UNREAD COUNT =================
    long countByUserAndReadFalse(UserEntity user);

    // ================= OPTIONAL (USEFUL) =================
    long countByUser_IdAndReadFalse(Long userId);
}