package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.io.ApiResponse;
import in.bawvpl.Authify.service.UserApplicationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1.0/user-app")
@RequiredArgsConstructor
@CrossOrigin("*")
@Slf4j
public class UserApplicationController {

    private final UserApplicationService userApplicationService;

    // ================= HELPER =================
    private String getEmail(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return auth.getName().toLowerCase().trim();
    }

    // ================= APPLY APP =================
    @PostMapping("/apply/{appId}")
    public ResponseEntity<?> applyApp(
            @PathVariable Long appId,
            Authentication authentication
    ) {

        var result = userApplicationService.applyApp(getEmail(authentication), appId);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("App applied successfully")
                        .data(result)
                        .build()
        );
    }

    // ================= GET USER APP =================
    @GetMapping("/{appId}")
    public ResponseEntity<?> getUserApp(
            @PathVariable Long appId,
            Authentication authentication
    ) {

        var result = userApplicationService.getUserApp(getEmail(authentication), appId);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("User app fetched")
                        .data(result)
                        .build()
        );
    }
}