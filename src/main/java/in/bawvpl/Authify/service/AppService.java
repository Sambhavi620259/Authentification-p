package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.AppEntity;
import in.bawvpl.Authify.repository.AppRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppService {

    private final AppRepository appRepository;

    // ================= CREATE =================
    @Transactional
    public AppEntity createApp(AppEntity app) {

        validateApp(app);

        AppEntity saved = appRepository.save(app);

        log.info("App created: {}", saved.getAppName());

        return saved;
    }

    // ================= GET ALL =================
    public List<AppEntity> getAllApps() {

        List<AppEntity> list = appRepository.findAll();

        log.info("Fetched {} apps", list.size());

        return list;
    }

    // ================= GET ONE =================
    public AppEntity getApp(Long id) {

        return appRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "App not found")
                );
    }

    // ================= UPDATE =================
    @Transactional
    public AppEntity updateApp(Long id, AppEntity updated) {

        AppEntity app = getApp(id);

        if (updated.getAppName() != null && !updated.getAppName().isBlank()) {
            app.setAppName(updated.getAppName());
        }

        if (updated.getAppType() != null) {
            app.setAppType(updated.getAppType());
        }

        if (updated.getAppText() != null) {
            app.setAppText(updated.getAppText());
        }

        if (updated.getAppUrl() != null && !updated.getAppUrl().isBlank()) {
            app.setAppUrl(updated.getAppUrl());
        }

        if (updated.getAppLogo() != null) {
            app.setAppLogo(updated.getAppLogo());
        }

        if (updated.getStatus() != null && !updated.getStatus().isBlank()) {
            app.setStatus(updated.getStatus());
        }

        AppEntity saved = appRepository.save(app);

        log.info("App updated: {}", saved.getAppName());

        return saved;
    }

    // ================= DELETE =================
    @Transactional
    public void deleteApp(Long id) {

        AppEntity app = getApp(id);

        appRepository.delete(app);

        log.info("App deleted: {}", app.getAppName());
    }

    // ================= VALIDATION =================
    private void validateApp(AppEntity app) {

        if (app.getAppName() == null || app.getAppName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "App name required");
        }

        if (app.getAppUrl() == null || app.getAppUrl().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "App URL required");
        }
    }
}