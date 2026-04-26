package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.NotificationEntity;
import in.bawvpl.Authify.io.ApiResponse;
import in.bawvpl.Authify.service.NotificationService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1.0/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // ================= LIST =================
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<NotificationEntity>>> getNotifications(
            @PathVariable Long userId
    ) {

        return ResponseEntity.ok(
                ApiResponse.<List<NotificationEntity>>builder()
                        .status(200)
                        .message("Notifications fetched")
                        .data(notificationService.getNotifications(userId))
                        .build()
        );
    }

    // ================= MARK READ =================
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Object>> markRead(@PathVariable Long id) {

        notificationService.markAsRead(id);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("Marked as read")
                        .data(null)
                        .build()
        );
    }

    // ================= CREATE (OPTIONAL ADMIN/API USE) =================
    @PostMapping
    public ResponseEntity<ApiResponse<Object>> create(
            @RequestParam Long userId,
            @RequestParam String title,
            @RequestParam String message
    ) {

        notificationService.create(userId, title, message);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("Notification created")
                        .data(null)
                        .build()
        );
    }

    // ================= UNREAD COUNT =================
    @GetMapping("/{userId}/unread-count")
    public ResponseEntity<ApiResponse<Long>> unreadCount(@PathVariable Long userId) {

        return ResponseEntity.ok(
                ApiResponse.<Long>builder()
                        .status(200)
                        .message("Unread count fetched")
                        .data(notificationService.getUnreadCount(userId))
                        .build()
        );
    }
}
