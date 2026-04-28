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

        final String normalizedEmail = email.toLowerCase().trim();

        UserEntity user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ApplicationEntity app = applicationRepository.findById(appId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "App not found"));

        // ✅ FIXED (appId field)
        if (favoriteRepository.existsByUser_IdAndApp_AppId(user.getId(), appId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Already in favorites");
        }

        FavoriteEntity fav = FavoriteEntity.builder()
                .user(user)
                .app(app)
                .build();

        favoriteRepository.save(fav);

        log.info("Added to favorites: user={}, appId={}", normalizedEmail, appId);
    }

    // ================= GET =================
    public List<FavoriteResponse> get(String email) {

        final String normalizedEmail = email.toLowerCase().trim();

        UserEntity user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<FavoriteEntity> list = favoriteRepository.findByUser_Id(user.getId());

        return list.stream()
                .map(f -> FavoriteResponse.builder()
                        .appId(f.getApp().getAppId())   // ✅ FIXED
                        .appName(f.getApp().getAppName())
                        .appLogo(f.getApp().getAppLogo())
                        .appUrl(f.getApp().getAppUrl())
                        .build()
                )
                .collect(Collectors.toList()); // ✅ FIXED
    }

    // ================= REMOVE =================
    @Transactional
    public void remove(String email, Long appId) {

        final String normalizedEmail = email.toLowerCase().trim();

        UserEntity user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // ✅ FIXED
        if (!favoriteRepository.existsByUser_IdAndApp_AppId(user.getId(), appId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Favorite not found");
        }

        favoriteRepository.deleteByUser_IdAndApp_AppId(user.getId(), appId);

        log.info("Removed from favorites: user={}, appId={}", normalizedEmail, appId);
    }
}