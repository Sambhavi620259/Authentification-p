package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.ApplicationEntity;
import in.bawvpl.Authify.entity.UserApplicationEntity;
import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.repository.ApplicationRepository;
import in.bawvpl.Authify.repository.UserApplicationRepository;
import in.bawvpl.Authify.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

        final String normalizedEmail = normalizeEmail(email);

        UserEntity user = getUserByEmail(normalizedEmail);
        ApplicationEntity app = getAppById(appId);

        if (!"ACTIVE".equalsIgnoreCase(app.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "App not active");
        }

        if (userAppRepository.existsByUser_IdAndApp_AppId(user.getId(), appId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User already applied for this app"
            );
        }

        UserApplicationEntity entity = UserApplicationEntity.builder()
                .user(user)
                .app(app)
                .subscriptionStatus(STATUS_APPLIED)
                .visitCounter(0)
                .build();

        return userAppRepository.save(entity);
    }

    // ================= GET USER APP =================
    public UserApplicationEntity getUserApp(String email, Long appId) {

        final String normalizedEmail = normalizeEmail(email);

        UserEntity user = getUserByEmail(normalizedEmail);

        return userAppRepository
                .findByUser_IdAndApp_AppId(user.getId(), appId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found")
                );
    }

    // ================= GET ALL APPS (FIXED) =================
    public List<ApplicationEntity> getAllApps() {

        Pageable pageable = PageRequest.of(0, 50); // default limit

        return applicationRepository
                .findByStatus("ACTIVE", pageable)
                .getContent();
    }

    // ================= GET USER APPLICATIONS =================
    public List<UserApplicationEntity> getUserApplications(String email) {

        final String normalizedEmail = normalizeEmail(email);

        UserEntity user = getUserByEmail(normalizedEmail);

        return userAppRepository.findAllByUser_Id(user.getId());
    }

    // ================= HELPERS =================

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email required");
        }
        return email.toLowerCase().trim();
    }

    private UserEntity getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
                );
    }

    private ApplicationEntity getAppById(Long appId) {
        if (appId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "appId required");
        }

        return applicationRepository.findById(appId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "App not found")
                );
    }
}