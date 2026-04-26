package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.NotificationEntity;
import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.repository.NotificationRepository;
import in.bawvpl.Authify.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // ================= GET USER NOTIFICATIONS =================
    public List<NotificationEntity> getNotifications(Long userId) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
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
                .build(); // read & createdAt auto

        notificationRepository.save(n);
    }

    // ================= UNREAD COUNT =================
    public long getUnreadCount(Long userId) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationRepository.countByUserAndReadFalse(user);
    }
}
