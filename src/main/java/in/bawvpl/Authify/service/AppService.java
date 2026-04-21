package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.ApplicationEntity;
import in.bawvpl.Authify.repository.ApplicationRepository;

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

    private final ApplicationRepository applicationRepository;

    // ================= CREATE =================
    @Transactional
    public ApplicationEntity createApp(ApplicationEntity app) {

        validateApp(app);

        ApplicationEntity saved = applicationRepository.save(app);

        log.info("App created: {}", saved.getAppName());

        return saved;
    }

    // ================= GET ALL =================
    public List<ApplicationEntity> getAllApps() {

        List<ApplicationEntity> list = applicationRepository.findAll();

        log.info("Fetched {} apps", list.size());

        return list;
    }

    // ================= GET ONE =================
    public ApplicationEntity getApp(Long id) {

        return applicationRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "App not found")
                );
    }

    // ================= UPDATE =================
    @Transactional
    public ApplicationEntity updateApp(Long id, ApplicationEntity updated) {

        ApplicationEntity app = getApp(id);

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

        ApplicationEntity saved = applicationRepository.save(app);

        log.info("App updated: {}", saved.getAppName());

        return saved;
    }

    // ================= DELETE =================
    @Transactional
    public void deleteApp(Long id) {

        ApplicationEntity app = getApp(id);

        applicationRepository.delete(app);

        log.info("App deleted: {}", app.getAppName());
    }

    // ================= VALIDATION =================
    private void validateApp(ApplicationEntity app) {

        if (app.getAppName() == null || app.getAppName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "App name required");
        }

        if (app.getAppUrl() == null || app.getAppUrl().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "App URL required");
        }
    }
}