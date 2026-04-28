package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.*;
import in.bawvpl.Authify.io.*;
import in.bawvpl.Authify.repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppService {

    private final ApplicationRepository applicationRepository;
    private final UserApplicationRepository userAppRepo;
    private final UserRepository userRepository;

    // ================= CREATE =================
    @Transactional
    public ApplicationResponse createApp(Map<String, Object> body) {

        ApplicationEntity app = new ApplicationEntity();

        app.setAppName((String) body.get("appName"));
        app.setAppType((String) body.get("appType"));
        app.setAppText((String) body.get("appText"));
        app.setAppUrl((String) body.get("appUrl"));
        app.setAppLogo((String) body.get("appLogo"));
        app.setStatus("ACTIVE");

        validateApp(app);

        return toResponse(applicationRepository.save(app));
    }

    // ================= GET ALL =================
    public Page<ApplicationResponse> getAllApps(int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("appId").descending());

        return applicationRepository
                .findByStatus("ACTIVE", pageable)
                .map(this::toResponse);
    }

    // ================= GET ONE =================
    public ApplicationResponse getApp(Long id) {

        return applicationRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "App not found"));
    }

    // ================= GET USER APPS =================
    public Page<MyAppResponse> getAppsByUser(String email, int page, int size) {

        final String normalizedEmail = email.toLowerCase().trim();

        UserEntity user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());

        return userAppRepo.findAllByUser_Id(user.getId(), pageable)
                .map(ua -> MyAppResponse.builder()
                        .appId(ua.getApp().getAppId())
                        .appName(ua.getApp().getAppName())
                        .appLogo(ua.getApp().getAppLogo())
                        .appUrl(ua.getApp().getAppUrl())
                        .visitCounter(
                                ua.getVisitCounter() == null ? 0 : ua.getVisitCounter()
                        )
                        .subscriptionStatus(ua.getSubscriptionStatus())
                        .updatedAt(ua.getUpdatedAt())
                        .build()
                );
    }

    // ================= UPDATE =================
    @Transactional
    public ApplicationResponse updateApp(Long id, Map<String, Object> body) {

        ApplicationEntity app = applicationRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "App not found"));

        if (body.get("appName") != null)
            app.setAppName((String) body.get("appName"));

        if (body.get("appType") != null)
            app.setAppType((String) body.get("appType"));

        if (body.get("appText") != null)
            app.setAppText((String) body.get("appText"));

        if (body.get("appUrl") != null)
            app.setAppUrl((String) body.get("appUrl"));

        if (body.get("appLogo") != null)
            app.setAppLogo((String) body.get("appLogo"));

        if (body.get("status") != null)
            app.setStatus((String) body.get("status"));

        return toResponse(applicationRepository.save(app));
    }

    // ================= DELETE =================
    @Transactional
    public void deleteApp(Long id) {

        ApplicationEntity app = applicationRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "App not found"));

        app.setStatus("DELETED");

        applicationRepository.save(app);
    }

    // ================= OPEN APP =================
    @Transactional
    public void openApp(Long appId, String email) {

        final String normalizedEmail = email.toLowerCase().trim();

        UserEntity user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ApplicationEntity app = applicationRepository.findById(appId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "App not found"));

        if (!"ACTIVE".equalsIgnoreCase(app.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "App not active");
        }

        UserApplicationEntity ua = userAppRepo
                .findByUser_IdAndApp_AppId(user.getId(), appId)
                .orElse(null);

        if (ua != null) {
            ua.setVisitCounter(
                    ua.getVisitCounter() == null ? 1 : ua.getVisitCounter() + 1
            );
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
    }

    // ================= MAPPER =================
    private ApplicationResponse toResponse(ApplicationEntity app) {
        return ApplicationResponse.builder()
                .appId(app.getAppId())
                .appName(app.getAppName())
                .appType(app.getAppType())
                .appUrl(app.getAppUrl())
                .appLogo(app.getAppLogo())
                .status(app.getStatus())
                .build();
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