package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.*;
import in.bawvpl.Authify.io.SettingsRequest;
import in.bawvpl.Authify.io.SettingsResponse;
import in.bawvpl.Authify.repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettingsService {

    private final SettingsRepository settingsRepo;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 🔥 NEW (enterprise features)
    private final UserSessionRepository sessionRepo;
    private final AuditLogRepository auditRepo;

    // ================= GET =================
    public SettingsResponse get(String email) {

        UserEntity user = getUser(email);
        Settings settings = getOrCreate(user);

        return mapToResponse(settings);
    }

    // ================= UPDATE =================
    @Transactional
    public void update(String email, SettingsRequest req) {

        UserEntity user = getUser(email);
        Settings settings = getOrCreate(user);

        if (req.getNotificationsEnabled() != null) {
            settings.setNotificationsEnabled(req.getNotificationsEnabled());
        }

        if (req.getEmailAlerts() != null) {
            settings.setEmailAlerts(req.getEmailAlerts());
        }

        if (req.getDarkMode() != null) {
            settings.setDarkMode(req.getDarkMode());
        }

        settingsRepo.save(settings);
    }

    // ================= CHANGE PASSWORD =================
    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {

        UserEntity user = getUser(email);

        if (currentPassword == null || newPassword == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password required");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        audit(user.getId(), "PASSWORD_CHANGED", null);
    }

    // ================= LOGOUT ALL =================
    @Transactional
    public void logoutAll(String email) {

        UserEntity user = getUser(email);

        Integer version = user.getTokenVersion() == null ? 0 : user.getTokenVersion();
        user.setTokenVersion(version + 1);

        userRepository.save(user);

        audit(user.getId(), "LOGOUT_ALL", null);
    }

    // ================= DEACTIVATE =================
    @Transactional
    public void deactivate(String email) {

        UserEntity user = getUser(email);

        user.setUserStatus("DEACTIVATED");
        userRepository.save(user);

        audit(user.getId(), "ACCOUNT_DEACTIVATED", null);
    }

    // ================= SESSION HISTORY =================
    public List<UserSession> getSessions(String email) {

        UserEntity user = getUser(email);

        return sessionRepo.findByUserIdAndActiveTrue(user.getId());
    }

    // ================= REVOKE SESSION =================
    @Transactional
    public void revokeSession(String email, Long sessionId) {

        UserEntity user = getUser(email);

        UserSession session = sessionRepo.findByIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        session.setActive(false);
        sessionRepo.save(session);

        audit(user.getId(), "SESSION_REVOKED", "sessionId=" + sessionId);
    }

    // ================= EXPORT =================
    public Object exportData(String email) {

        UserEntity user = getUser(email);

        return Map.of(
                "profile", user,
                "settings", getOrCreate(user),
                "sessions", sessionRepo.findByUserIdAndActiveTrue(user.getId())
        );
    }

    // ================= RE-AUTH =================
    public void reAuth(String email, String password) {

        UserEntity user = getUser(email);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password");
        }
    }

    // ================= 2FA ENABLE =================
    @Transactional
    public String enable2FA(String email) {

        UserEntity user = getUser(email);

        String secret = UUID.randomUUID().toString();

        user.setTwoFactorEnabled(true);
        user.setTwoFactorSecret(secret);

        userRepository.save(user);

        audit(user.getId(), "2FA_ENABLED", null);

        return secret;
    }

    // ================= 2FA DISABLE =================
    @Transactional
    public void disable2FA(String email) {

        UserEntity user = getUser(email);

        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);

        userRepository.save(user);

        audit(user.getId(), "2FA_DISABLED", null);
    }

    // ================= AUDIT =================
    private void audit(Long userId, String action, String meta) {

        auditRepo.save(
                AuditLog.builder()
                        .userId(userId)
                        .action(action)
                        .metadata(meta)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    // ================= GET OR CREATE =================
    public Settings getOrCreate(UserEntity user) {

        return settingsRepo.findByUser(user)
                .orElseGet(() -> settingsRepo.save(
                        Settings.builder()
                                .user(user)
                                .notificationsEnabled(true)
                                .emailAlerts(true)
                                .darkMode(false)
                                .build()
                ));
    }

    // ================= MAPPER =================
    private SettingsResponse mapToResponse(Settings s) {

        return SettingsResponse.builder()
                .notificationsEnabled(s.getNotificationsEnabled())
                .emailAlerts(s.getEmailAlerts())
                .darkMode(s.getDarkMode())
                .build();
    }

    // ================= USER HELPER =================
    private UserEntity getUser(String email) {

        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        return userRepository.findByEmailIgnoreCase(email.toLowerCase().trim())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}