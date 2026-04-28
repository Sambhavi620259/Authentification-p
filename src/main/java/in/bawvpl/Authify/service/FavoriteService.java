package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.*;
import in.bawvpl.Authify.repository.*;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;

    // ================= ADD =================
    public void add(String email, Long appId) {

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ApplicationEntity app = applicationRepository.findById(appId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "App not found"));

        if (favoriteRepository.existsByUserAndApp(user, app)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Already in favorites");
        }

        FavoriteEntity fav = FavoriteEntity.builder()
                .user(user)
                .app(app)
                .build();

        favoriteRepository.save(fav);
    }

    // ================= GET =================
    public List<FavoriteEntity> get(String email) {

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return favoriteRepository.findByUserOrderByCreatedAtDesc(user);
    }

    // ================= REMOVE =================
    public void remove(String email, Long appId) {

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ApplicationEntity app = applicationRepository.findById(appId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "App not found"));

        favoriteRepository.deleteByUserAndApp(user, app);
    }
}