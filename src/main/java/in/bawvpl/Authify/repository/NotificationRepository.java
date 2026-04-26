package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.NotificationEntity;
import in.bawvpl.Authify.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    // Existing
    List<NotificationEntity> findByUserOrderByCreatedAtDesc(UserEntity user);

    long countByUserAndReadFalse(UserEntity user);

    // 🔥 ADD THIS (FIX ERROR)
    Page<NotificationEntity> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}