package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.ApplicationEntity;
import in.bawvpl.Authify.io.ApiResponse;
import in.bawvpl.Authify.service.AppService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

    // ================= CREATE (ADMIN ONLY) =================
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody ApplicationEntity app) {

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("App created successfully")
                        .data(applicationService.createApp(app))
                        .build()
        );
    }

    // ================= GET ALL (FRONTEND) =================
    @GetMapping("/list")
    public ResponseEntity<?> list() {

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("Apps fetched")
                        .data(applicationService.getAllApps())
                        .build()
        );
    }

    // ================= MY APPS =================
    @GetMapping("/my")
    public ResponseEntity<?> myApps(Authentication auth) {

        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        String email = auth.getName();

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("My apps fetched")
                        .data(applicationService.getAppsByUser(email))
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
            @RequestBody ApplicationEntity app
    ) {

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("App updated successfully")
                        .data(applicationService.updateApp(id, app))
                        .build()
        );
    }

    // ================= DELETE (ADMIN ONLY) =================
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

        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        Long appId = body.get("appId");

        if (appId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "appId is required");
        }

        applicationService.openApp(appId, auth.getName());

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("App opened successfully")
                        .data(null)
                        .build()
        );
    }
}