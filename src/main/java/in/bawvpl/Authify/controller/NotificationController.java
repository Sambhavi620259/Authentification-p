package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.NotificationEntity;
import in.bawvpl.Authify.io.ApiResponse;
import in.bawvpl.Authify.service.NotificationService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1.0/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // ================= LIST (PAGINATION) =================
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getNotifications(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Page<NotificationEntity> data =
                notificationService.getNotifications(auth.getName(), page, size);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("Notifications fetched")
                        .data(data.getContent())
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

    // ================= UNREAD COUNT =================
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> unreadCount(Authentication auth) {

        long count = notificationService.getUnreadCountByEmail(auth.getName());

        return ResponseEntity.ok(
                ApiResponse.<Long>builder()
                        .status(200)
                        .message("Unread count fetched")
                        .data(count)
                        .build()
        );
    }
}