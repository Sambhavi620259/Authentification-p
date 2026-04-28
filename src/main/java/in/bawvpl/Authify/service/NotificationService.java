package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.NotificationEntity;
import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.repository.NotificationRepository;
import in.bawvpl.Authify.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // ================= GET USER NOTIFICATIONS (PAGINATION + SECURE) =================
    public Page<NotificationEntity> getNotifications(String email, int page, int size) {

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return notificationRepository.findByUser_IdOrderByCreatedAtDesc(
                user.getId(),
                pageable
        );
    }

    // ================= MARK AS READ =================
    public void markAsRead(Long notificationId) {

        NotificationEntity n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        n.setRead(true);
        notificationRepository.save(n);
    }

    // ================= CREATE =================
    public void create(Long userId, String title, String message) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        NotificationEntity n = NotificationEntity.builder()
                .user(user)
                .title(title)
                .message(message)
                .build();

        notificationRepository.save(n);
    }

    // ================= UNREAD COUNT (SECURE) =================
    public long getUnreadCountByEmail(String email) {

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationRepository.countByUserAndReadFalse(user);
    }
}