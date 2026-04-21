package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.ApplicationEntity;
import in.bawvpl.Authify.io.ApiResponse;
import in.bawvpl.Authify.service.AppService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

        ApplicationEntity saved = applicationService.createApp(app);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("App created successfully")
                        .data(saved)
                        .build()
        );
    }

    // ================= GET ALL =================
    @GetMapping
    public ResponseEntity<?> all() {

        List<ApplicationEntity> list = applicationService.getAllApps();

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("Apps fetched")
                        .data(list)
                        .build()
        );
    }

    // ================= GET ONE =================
    @GetMapping("/{id}")
    public ResponseEntity<?> one(@PathVariable Long id) {

        ApplicationEntity app = applicationService.getApp(id);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("App fetched")
                        .data(app)
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

        ApplicationEntity updated = applicationService.updateApp(id, app);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("App updated successfully")
                        .data(updated)
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
}