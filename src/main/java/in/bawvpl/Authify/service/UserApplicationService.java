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
    private final ApplicationRepository applicationRepository;

    private static final String STATUS_APPLIED = "APPLIED";

    // ================= APPLY APP =================
    @Transactional
    public UserApplicationEntity applyApp(String email, Long appId) {

        email = normalizeEmail(email);

        UserEntity user = getUserByEmail(email);
        ApplicationEntity app = getAppById(appId);

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

        return userAppRepository
                .findByUser_IdAndApp_AppId(user.getId(), appId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found")
                );
    }

    // ================= GET ALL APPS =================
    public List<ApplicationEntity> getAllApps() {
        return applicationRepository.findAll();
    }

    // ================= GET USER APPLICATIONS =================
    public List<UserApplicationEntity> getUserApplications(String email) {

        email = normalizeEmail(email);

        UserEntity user = getUserByEmail(email);

        return userAppRepository.findAllByUser(user);
    }

    // ================= HELPERS =================

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

    private ApplicationEntity getAppById(Long appId) {
        return applicationRepository.findById(appId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "App not found")
                );
    }
}