package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.io.ApiResponse;
import in.bawvpl.Authify.io.SettingsRequest;
import in.bawvpl.Authify.io.SettingsResponse;
import in.bawvpl.Authify.service.SettingsService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Map;

@RestController
@RequestMapping("/api/v1.0/settings")
@RequiredArgsConstructor
@CrossOrigin("*")
public class SettingsController {

    private final SettingsService settingsService;

    // ================= HELPER =================
    private String getEmail(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return auth.getName().toLowerCase().trim();
    }

    // ================= GET SETTINGS =================
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<SettingsResponse>> get(Authentication auth) {

        SettingsResponse res = settingsService.get(getEmail(auth));

        return ResponseEntity.ok(
                ApiResponse.<SettingsResponse>builder()
                        .status(200)
                        .message("Settings fetched")
                        .data(res)
                        .build()
        );
    }

    // ================= UPDATE SETTINGS =================
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<String>> update(
            Authentication auth,
            @RequestBody SettingsRequest req
    ) {

        if (req == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body required");
        }

        settingsService.update(getEmail(auth), req);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .status(200)
                        .message("Settings updated")
                        .data(null)
                        .build()
        );
    }

    // ================= CHANGE PASSWORD =================
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            Authentication auth,
            @RequestBody Map<String, String> req
    ) {

        if (req == null ||
                !req.containsKey("currentPassword") ||
                !req.containsKey("newPassword")) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
        }

        settingsService.changePassword(
                getEmail(auth),
                req.get("currentPassword"),
                req.get("newPassword")
        );

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .status(200)
                        .message("Password updated")
                        .data(null)
                        .build()
        );
    }

    // ================= LOGOUT ALL DEVICES =================
    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<String>> logoutAll(Authentication auth) {

        settingsService.logoutAll(getEmail(auth));

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .status(200)
                        .message("Logged out from all devices")
                        .data(null)
                        .build()
        );
    }

    // ================= DEACTIVATE ACCOUNT =================
    @PutMapping("/deactivate")
    public ResponseEntity<ApiResponse<String>> deactivate(Authentication auth) {

        settingsService.deactivate(getEmail(auth));

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .status(200)
                        .message("Account deactivated")
                        .data(null)
                        .build()
        );
    }

    // ================= SESSION HISTORY =================
    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<Object>> sessions(Authentication auth) {

        Object sessions = settingsService.getSessions(getEmail(auth));

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("Sessions fetched")
                        .data(sessions)
                        .build()
        );
    }

    // ================= REVOKE SINGLE SESSION =================
    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<ApiResponse<String>> revokeSession(
            Authentication auth,
            @PathVariable Long id
    ) {

        settingsService.revokeSession(getEmail(auth), id);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .status(200)
                        .message("Session revoked")
                        .data(null)
                        .build()
        );
    }

    // ================= RE-AUTH (SECURITY STEP) =================
    @PostMapping("/re-auth")
    public ResponseEntity<ApiResponse<String>> reAuth(
            Authentication auth,
            @RequestBody Map<String, String> req
    ) {

        if (req == null || !req.containsKey("password")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password required");
        }

        settingsService.reAuth(getEmail(auth), req.get("password"));

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .status(200)
                        .message("Re-auth successful")
                        .data(null)
                        .build()
        );
    }

    // ================= 2FA ENABLE =================
    @PostMapping("/2fa/enable")
    public ResponseEntity<ApiResponse<Object>> enable2FA(Authentication auth) {

        String secret = settingsService.enable2FA(getEmail(auth));

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("2FA enabled")
                        .data(Map.of("secret", secret))
                        .build()
        );
    }

    // ================= 2FA DISABLE =================
    @PostMapping("/2fa/disable")
    public ResponseEntity<ApiResponse<String>> disable2FA(Authentication auth) {

        settingsService.disable2FA(getEmail(auth));

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .status(200)
                        .message("2FA disabled")
                        .data(null)
                        .build()
        );
    }

    // ================= DATA EXPORT =================
    @GetMapping("/export")
    public ResponseEntity<ApiResponse<Object>> export(Authentication auth) {

        Object data = settingsService.exportData(getEmail(auth));

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("User data exported")
                        .data(data)
                        .build()
        );
    }
}