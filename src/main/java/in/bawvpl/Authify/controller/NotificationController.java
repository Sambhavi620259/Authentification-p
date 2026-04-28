package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.NotificationEntity;
import in.bawvpl.Authify.io.ApiResponse;
import in.bawvpl.Authify.service.NotificationService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/v1.0/notifications")
@RequiredArgsConstructor
@CrossOrigin("*")
public class NotificationController {

    private final NotificationService notificationService;

    // ================= HELPER =================
    private String getEmail(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return auth.getName().toLowerCase().trim();
    }

    // ================= GET LIST =================
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getNotifications(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Page<NotificationEntity> pageData =
                notificationService.getNotifications(getEmail(auth), page, size);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("Notifications fetched")
                        .data(pageData.getContent())
                        .meta(Map.of(
                                "page", page,
                                "size", size,
                                "totalPages", pageData.getTotalPages(),
                                "totalElements", pageData.getTotalElements()
                        ))
                        .build()
        );
    }

    // ================= MARK AS READ =================
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Object>> markAsRead(
            @PathVariable Long id,
            Authentication auth
    ) {

        notificationService.markAsRead(id, getEmail(auth));

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
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(Authentication auth) {

        long count = notificationService.getUnreadCountByEmail(getEmail(auth));

        return ResponseEntity.ok(
                ApiResponse.<Long>builder()
                        .status(200)
                        .message("Unread count fetched")
                        .data(count)
                        .build()
        );
    }
}