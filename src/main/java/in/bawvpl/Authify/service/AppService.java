package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.ApplicationEntity;
import in.bawvpl.Authify.entity.UserApplicationEntity;
import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.repository.ApplicationRepository;
import in.bawvpl.Authify.repository.UserApplicationRepository;
import in.bawvpl.Authify.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppService {

    private final ApplicationRepository applicationRepository;
    private final UserApplicationRepository userAppRepo;   // 🔥 added
    private final UserRepository userRepository;           // 🔥 added

    // ================= CREATE =================
    @Transactional
    public ApplicationEntity createApp(ApplicationEntity app) {

        validateApp(app);

        ApplicationEntity saved = applicationRepository.save(app);

        log.info("App created: {}", saved.getAppName());

        return saved;
    }

    // ================= GET ALL =================
    public List<ApplicationEntity> getAllApps() {

        List<ApplicationEntity> list = applicationRepository.findAll();

        log.info("Fetched {} apps", list.size());

        return list;
    }

    // ================= GET ONE =================
    public ApplicationEntity getApp(Long id) {

        return applicationRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "App not found")
                );
    }

    // ================= UPDATE =================
    @Transactional
    public ApplicationEntity updateApp(Long id, ApplicationEntity updated) {

        ApplicationEntity app = getApp(id);

        if (updated.getAppName() != null && !updated.getAppName().isBlank()) {
            app.setAppName(updated.getAppName());
        }

        if (updated.getAppType() != null) {
            app.setAppType(updated.getAppType());
        }

        if (updated.getAppText() != null) {
            app.setAppText(updated.getAppText());
        }

        if (updated.getAppUrl() != null && !updated.getAppUrl().isBlank()) {
            app.setAppUrl(updated.getAppUrl());
        }

        if (updated.getAppLogo() != null) {
            app.setAppLogo(updated.getAppLogo());
        }

        if (updated.getStatus() != null && !updated.getStatus().isBlank()) {
            app.setStatus(updated.getStatus());
        }

        ApplicationEntity saved = applicationRepository.save(app);

        log.info("App updated: {}", saved.getAppName());

        return saved;
    }

    // ================= DELETE =================
    @Transactional
    public void deleteApp(Long id) {

        ApplicationEntity app = getApp(id);

        applicationRepository.delete(app);

        log.info("App deleted: {}", app.getAppName());
    }

    // ================= OPEN APP (🔥 FIXED) =================
    @Transactional
    public void openApp(Long appId) {

        // 🔥 get logged-in user from JWT
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ApplicationEntity app = getApp(appId);

        // 🔥 check existing record
        UserApplicationEntity ua = userAppRepo
                .findByUser_IdAndApp_AppId(user.getId(), appId)
                .orElse(null);

        if (ua != null) {
            ua.setVisitCounter(ua.getVisitCounter() + 1);
            ua.setUpdatedAt(LocalDateTime.now());
        } else {
            ua = UserApplicationEntity.builder()
                    .user(user)
                    .app(app)
                    .visitCounter(1)
                    .subscriptionStatus("ACTIVE")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
        }

        userAppRepo.save(ua);

        log.info("App opened by user {} for app {}", user.getId(), appId);
    }

    // ================= VALIDATION =================
    private void validateApp(ApplicationEntity app) {

        if (app.getAppName() == null || app.getAppName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "App name required");
        }

        if (app.getAppUrl() == null || app.getAppUrl().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "App URL required");
        }
    }
}