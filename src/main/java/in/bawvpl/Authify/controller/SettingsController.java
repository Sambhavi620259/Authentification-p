package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.io.ApiResponse;
import in.bawvpl.Authify.io.SettingsRequest;
import in.bawvpl.Authify.io.SettingsResponse;
import in.bawvpl.Authify.service.SettingsService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1.0/settings")
@RequiredArgsConstructor
@CrossOrigin("*")
public class SettingsController {

    private final SettingsService settingsService;

    // ================= GET =================
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<SettingsResponse>> get(Authentication auth) {

        SettingsResponse res = settingsService.get(auth.getName());

        return ResponseEntity.ok(
                ApiResponse.<SettingsResponse>builder()
                        .status(200)
                        .message("Settings fetched")
                        .data(res)
                        .build()
        );
    }

    // ================= UPDATE =================
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<String>> update(
            Authentication auth,
            @RequestBody SettingsRequest req
    ) {

        settingsService.update(auth.getName(), req);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .status(200)
                        .message("Settings updated")
                        .data(null)
                        .build()
        );
    }
}