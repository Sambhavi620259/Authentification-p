package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.ApplicationEntity;
import in.bawvpl.Authify.service.AppService;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1.0/application")
@RequiredArgsConstructor
@CrossOrigin("*")
@Slf4j
public class ApplicationController {

    private final AppService applicationService;

    // ================= CREATE =================
    @PostMapping
    public ResponseEntity<?> create(@RequestBody ApplicationEntity app) {

        try {
            ApplicationEntity saved = applicationService.createApp(app);

            return ResponseEntity.ok(Map.of(
                    "message", "App created",
                    "data", saved
            ));

        } catch (Exception e) {
            log.error("Create app error: {}", e.getMessage());

            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage()
            ));
        }
    }

    // ================= GET ALL =================
    @GetMapping
    public ResponseEntity<?> all() {

        List<ApplicationEntity> list = applicationService.getAllApps();

        return ResponseEntity.ok(Map.of(
                "message", "Apps fetched",
                "data", list
        ));
    }

    // ================= GET ONE =================
    @GetMapping("/{id}")
    public ResponseEntity<?> one(@PathVariable Long id) {

        try {
            ApplicationEntity app = applicationService.getApp(id);

            return ResponseEntity.ok(Map.of(
                    "message", "App fetched",
                    "data", app
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage()
            ));
        }
    }

    // ================= UPDATE =================
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody ApplicationEntity app
    ) {

        try {
            ApplicationEntity updated = applicationService.updateApp(id, app);

            return ResponseEntity.ok(Map.of(
                    "message", "App updated",
                    "data", updated
            ));

        } catch (Exception e) {
            log.error("Update app error: {}", e.getMessage());

            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage()
            ));
        }
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {

        try {
            applicationService.deleteApp(id);

            return ResponseEntity.ok(Map.of(
                    "message", "Deleted successfully"
            ));

        } catch (Exception e) {
            log.error("Delete app error: {}", e.getMessage());

            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage()
            ));
        }
    }
}