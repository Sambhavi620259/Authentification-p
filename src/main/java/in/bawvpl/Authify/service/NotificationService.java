package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.NotificationEntity;
import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.repository.NotificationRepository;
import in.bawvpl.Authify.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // ================= GET USER NOTIFICATIONS =================
    public Page<NotificationEntity> getNotifications(String email, int page, int size) {

        final String normalizedEmail = normalizeEmail(email);

        UserEntity user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return notificationRepository.findByUser_IdOrderByCreatedAtDesc(
                user.getId(),
                pageable
        );
    }

    // ================= MARK AS READ (SECURE) =================
    public void markAsRead(Long notificationId, String email) {

        final String normalizedEmail = normalizeEmail(email);

        UserEntity user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        NotificationEntity n = notificationRepository.findById(notificationId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));

        // 🔥 SECURITY CHECK
        if (!n.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        n.setRead(true);
        notificationRepository.save(n);
    }

    // ================= CREATE =================
    public void create(Long userId, String title, String message) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        NotificationEntity n = NotificationEntity.builder()
                .user(user)
                .title(title)
                .message(message)
                .read(false)
                .build();

        notificationRepository.save(n);
    }

    // ================= UNREAD COUNT =================
    public long getUnreadCountByEmail(String email) {

        final String normalizedEmail = normalizeEmail(email);

        UserEntity user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return notificationRepository.countByUser_IdAndReadFalse(user.getId());
    }

    // ================= HELPER =================
    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email required");
        }
        return email.toLowerCase().trim();
    }
}