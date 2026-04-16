package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.service.UserApplicationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1.0/app")
@RequiredArgsConstructor
@CrossOrigin("*")
@Slf4j
public class UserApplicationController {

    private final UserApplicationService userApplicationService;

    // ================= APPLY APP =================
    @PostMapping("/apply")
    public ResponseEntity<?> applyApp(
            @RequestParam Long appId,
            Authentication authentication
    ) {

        try {
            String email = authentication.getName().toLowerCase().trim();

            var result = userApplicationService.applyApp(email, appId);

            return ResponseEntity.ok(Map.of(
                    "message", "App applied successfully",
                    "data", result
            ));

        } catch (Exception e) {
            log.error("Apply app error: {}", e.getMessage());

            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage()
            ));
        }
    }

    // ================= GET USER APP =================
    @GetMapping("/get")
    public ResponseEntity<?> getUserApp(
            @RequestParam Long appId,
            Authentication authentication
    ) {

        try {
            String email = authentication.getName().toLowerCase().trim();

            var result = userApplicationService.getUserApp(email, appId);

            return ResponseEntity.ok(Map.of(
                    "message", "User app fetched",
                    "data", result
            ));

        } catch (Exception e) {
            log.error("Get app error: {}", e.getMessage());

            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage()
            ));
        }
    }
}