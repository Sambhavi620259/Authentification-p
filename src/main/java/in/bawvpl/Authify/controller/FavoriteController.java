package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.io.ApiResponse;
import in.bawvpl.Authify.io.FavoriteResponse;
import in.bawvpl.Authify.service.FavoriteService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1.0/favorites")
@RequiredArgsConstructor
@CrossOrigin("*")
public class FavoriteController {

    private final FavoriteService favoriteService;

    // ================= HELPER =================
    private String getEmail(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return auth.getName().toLowerCase().trim();
    }

    // ================= ADD / TOGGLE =================
    @PostMapping("/{appId}")
    public ResponseEntity<ApiResponse<String>> toggleFavorite(
            Authentication auth,
            @PathVariable Long appId
    ) {

        if (appId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "appId required");
        }

        String message = favoriteService.toggleFavorite(getEmail(auth), appId);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .status(200)
                        .message(message) // "Added" or "Removed"
                        .data(null)
                        .build()
        );
    }

    // ================= GET LIST =================
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<FavoriteResponse>>> getFavorites(
            Authentication auth
    ) {

        List<FavoriteResponse> list =
                favoriteService.get(getEmail(auth));

        return ResponseEntity.ok(
                ApiResponse.<List<FavoriteResponse>>builder()
                        .status(200)
                        .message("Favorites fetched")
                        .data(list)
                        .build()
        );
    }

    // ================= REMOVE (OPTIONAL SEPARATE API) =================
    @DeleteMapping("/{appId}")
    public ResponseEntity<ApiResponse<String>> removeFavorite(
            Authentication auth,
            @PathVariable Long appId
    ) {

        if (appId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "appId required");
        }

        favoriteService.remove(getEmail(auth), appId);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .status(200)
                        .message("Removed from favorites")
                        .data(null)
                        .build()
        );
    }
}