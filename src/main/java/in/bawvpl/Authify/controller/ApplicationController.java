package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.AppEntity;
import in.bawvpl.Authify.service.AppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1.0/application")
@RequiredArgsConstructor
public class ApplicationController {

    private final AppService appService;

    // ✅ CREATE
    @PostMapping
    public AppEntity create(@RequestBody AppEntity app) {
        return appService.createApp(app);
    }

    // ✅ GET ALL
    @GetMapping
    public List<AppEntity> all() {
        return appService.getAllApps();
    }

    // ✅ GET ONE
    @GetMapping("/{id}")
    public AppEntity one(@PathVariable Long id) {
        return appService.getApp(id);
    }

    // ✅ UPDATE
    @PutMapping("/{id}")
    public AppEntity update(@PathVariable Long id, @RequestBody AppEntity app) {
        return appService.updateApp(id, app);
    }

    // ✅ DELETE
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        appService.deleteApp(id);
        return "Deleted Successfully";
    }
}