package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.io.ActivityResponse;
import in.bawvpl.Authify.io.ApiResponse;
import in.bawvpl.Authify.service.ActivityService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/v1.0/activity")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ActivityController {

    private final ActivityService activityService;

    // ================= HELPER =================
    private String getEmail(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return auth.getName().toLowerCase().trim();
    }

    // ================= GET USER ACTIVITIES =================
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getActivities(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Page<ActivityResponse> result =
                activityService.getActivities(getEmail(auth), page, size);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("Activities fetched")
                        .data(result.getContent())
                        .meta(Map.of(
                                "page", page,
                                "size", size,
                                "totalPages", result.getTotalPages(),
                                "totalElements", result.getTotalElements()
                        ))
                        .build()
        );
    }
}