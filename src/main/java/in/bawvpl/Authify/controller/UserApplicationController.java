package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.UserApplicationEntity;
import in.bawvpl.Authify.service.UserApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1.0/user-app")
@RequiredArgsConstructor
public class UserApplicationController {

    private final UserApplicationService service;

    // ✅ APPLY APP
    @PostMapping("/apply")
    public UserApplicationEntity apply(
            @RequestParam Long userId,
            @RequestParam Long appId
    ) {
        return service.applyApp(userId, appId); // ✅ FIXED
    }
}