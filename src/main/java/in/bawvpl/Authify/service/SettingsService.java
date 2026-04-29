package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.Settings;
import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.io.SettingsRequest;
import in.bawvpl.Authify.io.SettingsResponse;
import in.bawvpl.Authify.repository.SettingsRepository;
import in.bawvpl.Authify.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final SettingsRepository settingsRepo;
    private final UserRepository userRepository;

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

        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}