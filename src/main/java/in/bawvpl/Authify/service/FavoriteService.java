package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.*;
import in.bawvpl.Authify.io.FavoriteResponse;
import in.bawvpl.Authify.repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;

    // ================= ADD =================
    @Transactional
    public void add(String email, Long appId) {

        UserEntity user = getUser(email);
        ApplicationEntity app = getApp(appId);

        if (favoriteRepository.existsByUser_IdAndApp_AppId(user.getId(), appId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Already in favorites");
        }

        favoriteRepository.save(
                FavoriteEntity.builder()
                        .user(user)
                        .app(app)
                        .build()
        );

        log.info("Added to favorites: user={}, appId={}", user.getEmail(), appId);
    }

    // ================= TOGGLE (BEST UX) =================
    @Transactional
    public String toggleFavorite(String email, Long appId) {

        UserEntity user = getUser(email);
        ApplicationEntity app = getApp(appId);

        if (favoriteRepository.existsByUser_IdAndApp_AppId(user.getId(), appId)) {
            favoriteRepository.deleteByUser_IdAndApp_AppId(user.getId(), appId);
            log.info("Removed from favorites: user={}, appId={}", user.getEmail(), appId);
            return "Removed from favorites";
        }

        favoriteRepository.save(
                FavoriteEntity.builder()
                        .user(user)
                        .app(app)
                        .build()
        );

        log.info("Added to favorites: user={}, appId={}", user.getEmail(), appId);
        return "Added to favorites";
    }

    // ================= GET =================
    public List<FavoriteResponse> get(String email) {

        UserEntity user = getUser(email);

        return favoriteRepository.findByUser_Id(user.getId())
                .stream()
                .map(f -> FavoriteResponse.builder()
                        .appId(f.getApp().getAppId())
                        .appName(f.getApp().getAppName())
                        .appLogo(f.getApp().getAppLogo())
                        .appUrl(f.getApp().getAppUrl())
                        .build()
                )
                .collect(Collectors.toList());
    }

    // ================= REMOVE =================
    @Transactional
    public void remove(String email, Long appId) {

        UserEntity user = getUser(email);

        if (!favoriteRepository.existsByUser_IdAndApp_AppId(user.getId(), appId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Favorite not found");
        }

        favoriteRepository.deleteByUser_IdAndApp_AppId(user.getId(), appId);

        log.info("Removed from favorites: user={}, appId={}", user.getEmail(), appId);
    }

    // ================= HELPERS =================
    private UserEntity getUser(String email) {

        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email required");
        }

        return userRepository.findByEmailIgnoreCase(email.toLowerCase().trim())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private ApplicationEntity getApp(Long appId) {

        if (appId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "appId required");
        }

        return applicationRepository.findById(appId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "App not found"));
    }
}
