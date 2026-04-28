package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.io.ApiResponse;
import in.bawvpl.Authify.service.AppService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Map;

@RestController
@RequestMapping("/api/v1.0/application")
@RequiredArgsConstructor
@CrossOrigin("*")
@Slf4j
public class ApplicationController {

    private final AppService applicationService;

    // ================= HELPER =================
    private String getEmail(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return auth.getName().toLowerCase().trim();
    }

    // ================= CREATE (ADMIN ONLY) =================
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("App created successfully")
                        .data(applicationService.createApp(body))
                        .build()
        );
    }

    // ================= GET ALL (PAGINATED) =================
    @GetMapping("/list")
    public ResponseEntity<?> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Page<?> result = applicationService.getAllApps(page, size);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("Apps fetched")
                        .data(result.getContent())
                        .meta(Map.of(
                                "page", page,
                                "size", size,
                                "totalPages", result.getTotalPages(),
                                "totalElements", result.getTotalElements()
                        ))
                        .build()
        );
    }

    // ================= MY APPS (PAGINATED) =================
    @GetMapping("/my")
    public ResponseEntity<?> myApps(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Page<?> result = applicationService.getAppsByUser(getEmail(auth), page, size);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("My apps fetched")
                        .data(result.getContent())
                        .meta(Map.of(
                                "page", page,
                                "size", size,
                                "totalPages", result.getTotalPages(),
                                "totalElements", result.getTotalElements()
                        ))
                        .build()
        );
    }

    // ================= GET ONE =================
    @GetMapping("/{id}")
    public ResponseEntity<?> one(@PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("App fetched")
                        .data(applicationService.getApp(id))
                        .build()
        );
    }

    // ================= UPDATE (ADMIN ONLY) =================
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body
    ) {

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("App updated successfully")
                        .data(applicationService.updateApp(id, body))
                        .build()
        );
    }

    // ================= DELETE (SOFT DELETE) =================
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {

        applicationService.deleteApp(id);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("Deleted successfully")
                        .data(null)
                        .build()
        );
    }

    // ================= OPEN APP =================
    @PostMapping("/open")
    public ResponseEntity<?> openApp(
            Authentication auth,
            @RequestBody Map<String, Long> body
    ) {

        Long appId = body.get("appId");

        if (appId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "appId is required");
        }

        applicationService.openApp(appId, getEmail(auth));

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("App opened successfully")
                        .data(null)
                        .build()
        );
    }
}