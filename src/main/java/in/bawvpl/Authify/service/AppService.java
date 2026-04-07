package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.AppEntity;
import in.bawvpl.Authify.repository.AppRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppService {

    private final AppRepository appRepository;

    // ✅ CREATE
    public AppEntity createApp(AppEntity app) {
        return appRepository.save(app);
    }

    // ✅ GET ALL
    public List<AppEntity> getAllApps() {
        return appRepository.findAll();
    }

    // ✅ GET ONE
    public AppEntity getApp(Long id) {
        return appRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("App not found"));
    }

    // ✅ UPDATE
    public AppEntity updateApp(Long id, AppEntity updated) {

        AppEntity app = getApp(id);

        app.setAppType(updated.getAppType());
        app.setAppName(updated.getAppName());
        app.setAppText(updated.getAppText());
        app.setAppUrl(updated.getAppUrl());
        app.setAppLogo(updated.getAppLogo());

        return appRepository.save(app);
    }

    // ✅ DELETE
    public void deleteApp(Long id) {
        appRepository.deleteById(id);
    }
}