package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.AppEntity;
import in.bawvpl.Authify.entity.UserApplicationEntity;
import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.repository.AppRepository;
import in.bawvpl.Authify.repository.UserApplicationRepository;
import in.bawvpl.Authify.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserApplicationService {

    private final UserApplicationRepository userAppRepository;
    private final UserRepository userRepository;
    private final AppRepository appRepository;

    // ================= APPLY APP =================
    public UserApplicationEntity applyApp(Long userId, Long appId) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        AppEntity app = appRepository.findById(appId)
                .orElseThrow(() -> new RuntimeException("App not found"));

        Optional<UserApplicationEntity> existing =
                userAppRepository.findByUser_IdAndApp_AppId(userId, appId);

        if (existing.isPresent()) {
            throw new RuntimeException("User already applied for this app");
        }

        UserApplicationEntity entity = UserApplicationEntity.builder()
                .user(user)
                .app(app)
                .subscriptionStatus("APPLIED") // correct
                .build();

        return userAppRepository.save(entity);
    }

    // ================= GET USER APP =================
    public Optional<UserApplicationEntity> getUserApp(Long userId, Long appId) {
        return userAppRepository.findByUser_IdAndApp_AppId(userId, appId);
    }
}