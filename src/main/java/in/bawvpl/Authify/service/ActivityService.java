package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.ActivityLog;
import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.io.ActivityResponse;
import in.bawvpl.Authify.repository.ActivityLogRepository;
import in.bawvpl.Authify.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    // ================= LOG ACTIVITY =================
    public void log(String email, String action, String description) {

        final String normalizedEmail = normalizeEmail(email);

        UserEntity user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ActivityLog log = ActivityLog.builder()
                .user(user)
                .action(action)
                .description(description)
                .build();

        activityLogRepository.save(log);
    }

    // ================= GET ACTIVITIES =================
    public Page<ActivityResponse> getActivities(String email, int page, int size) {

        final String normalizedEmail = normalizeEmail(email);

        UserEntity user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());

        return activityLogRepository
                .findByUser_IdOrderByTimestampDesc(user.getId(), pageable)
                .map(this::toResponse);
    }

    // ================= MAPPER =================
    private ActivityResponse toResponse(ActivityLog log) {
        return ActivityResponse.builder()
                .action(log.getAction())
                .description(log.getDescription())
                .timestamp(log.getTimestamp())
                .build();
    }

    // ================= HELPER =================
    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email required");
        }
        return email.toLowerCase().trim();
    }
}