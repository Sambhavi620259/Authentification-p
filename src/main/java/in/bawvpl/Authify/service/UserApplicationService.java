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

@Service
@RequiredArgsConstructor
@Slf4j
public class UserApplicationService {

    private final UserApplicationRepository userAppRepository;
    private final UserRepository userRepository;
    private final AppRepository appRepository;

    // ✅ STATUS CONSTANT
    private static final String STATUS_APPLIED = "APPLIED";

    // ================= APPLY APP =================
    @Transactional
    public UserApplicationEntity applyApp(String email, Long appId) {

        email = email.toLowerCase().trim();

        try {
            // ✅ Get user
            UserEntity user = userRepository.findByEmail(email)
                    .orElseThrow(() ->
                            new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            // ✅ Get app
            AppEntity app = appRepository.findById(appId)
                    .orElseThrow(() ->
                            new ResponseStatusException(HttpStatus.NOT_FOUND, "App not found"));

            // ✅ Check duplicate
            userAppRepository.findByUser_IdAndApp_AppId(user.getId(), appId)
                    .ifPresent(existing -> {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "User already applied for this app"
                        );
                    });

            // ✅ Create application
            UserApplicationEntity entity = UserApplicationEntity.builder()
                    .user(user)
                    .app(app)
                    .subscriptionStatus(STATUS_APPLIED)
                    .build();

            UserApplicationEntity saved = userAppRepository.save(entity);

            log.info("User [{}] applied for app [{}]", email, appId);

            return saved;

        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Apply app failed for email={} appId={}", email, appId, ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Something went wrong"
            );
        }
    }

    // ================= GET USER APP =================
    public UserApplicationEntity getUserApp(String email, Long appId) {

        email = email.toLowerCase().trim();

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return userAppRepository.findByUser_IdAndApp_AppId(user.getId(), appId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
    }
}