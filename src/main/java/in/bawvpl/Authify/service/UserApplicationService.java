package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.AppEntity;
import in.bawvpl.Authify.entity.UserApplicationEntity;
import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.repository.AppRepository;
import in.bawvpl.Authify.repository.UserApplicationRepository;
import in.bawvpl.Authify.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserApplicationService {

    private final UserApplicationRepository userAppRepository;
    private final UserRepository userRepository;
    private final AppRepository appRepository;

    private static final String STATUS_APPLIED = "APPLIED";

    // ================= APPLY APP =================
    @Transactional
    public UserApplicationEntity applyApp(String email, Long appId) {

        email = normalizeEmail(email);

        UserEntity user = getUserByEmail(email);
        AppEntity app = getAppById(appId);

        // ✅ Duplicate check
        userAppRepository.findByUser_IdAndApp_AppId(user.getId(), appId)
                .ifPresent(existing -> {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "User already applied for this app"
                    );
                });

        UserApplicationEntity entity = UserApplicationEntity.builder()
                .user(user)
                .app(app)
                .subscriptionStatus(STATUS_APPLIED)
                .visitCounter(0)
                .build();

        UserApplicationEntity saved = userAppRepository.save(entity);

        log.info("User [{}] applied for app [{}]", email, appId);

        return saved;
    }

    // ================= GET USER APP =================
    public UserApplicationEntity getUserApp(String email, Long appId) {

        email = normalizeEmail(email);

        UserEntity user = getUserByEmail(email);

        UserApplicationEntity entity = userAppRepository
                .findByUser_IdAndApp_AppId(user.getId(), appId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found")
                );

        log.info("Fetched app [{}] for user [{}]", appId, email);

        return entity;
    }

    // ================= GET ALL APPS =================
    public List<AppEntity> getAllApps() {
        return appRepository.findAll();
    }

    // ================= GET USER APPLICATIONS =================
    public List<UserApplicationEntity> getUserApplications(String email) {

        email = normalizeEmail(email);

        UserEntity user = getUserByEmail(email);

        return userAppRepository.findAllByUser(user);
    }

    // ================= HELPER METHODS =================

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email required");
        }
        return email.toLowerCase().trim();
    }

    private UserEntity getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
                );
    }

    private AppEntity getAppById(Long appId) {
        return appRepository.findById(appId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "App not found")
                );
    }
}