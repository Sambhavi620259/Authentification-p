package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.FavoriteEntity;
import in.bawvpl.Authify.service.FavoriteService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1.0/favorites")
@RequiredArgsConstructor
@CrossOrigin("*")
public class FavoriteController {

    private final FavoriteService favoriteService;

    // ================= ADD =================
    @PostMapping("/{appId}")
    public ResponseEntity<?> add(Authentication auth, @PathVariable Long appId) {

        favoriteService.add(auth.getName(), appId);

        return ResponseEntity.ok(Map.of(
                "message", "Added to favorites"
        ));
    }

    // ================= GET =================
    @GetMapping
    public ResponseEntity<?> get(Authentication auth) {

        List<FavoriteEntity> list = favoriteService.get(auth.getName());

        return ResponseEntity.ok(Map.of(
                "message", "Favorites fetched",
                "data", list
        ));
    }

    // ================= REMOVE =================
    @DeleteMapping("/{appId}")
    public ResponseEntity<?> remove(Authentication auth, @PathVariable Long appId) {

        favoriteService.remove(auth.getName(), appId);

        return ResponseEntity.ok(Map.of(
                "message", "Removed from favorites"
        ));
    }
}