package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.service.UserApplicationService;
import in.bawvpl.Authify.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1.0/app")
@RequiredArgsConstructor
@CrossOrigin("*")
public class UserApplicationController {

    private final UserApplicationService userApplicationService;
    private final JwtUtil jwtUtil;

    @PostMapping("/apply")
    public ResponseEntity<?> applyApp(
            @RequestParam Long appId,
            HttpServletRequest request
    ) {
        try {
            String token = request.getHeader("Authorization").substring(7);
            String email = jwtUtil.extractUsername(token);

            return ResponseEntity.ok(
                    userApplicationService.applyApp(email, appId)
            );

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/get")
    public ResponseEntity<?> getUserApp(
            @RequestParam Long appId,
            HttpServletRequest request
    ) {
        try {
            String token = request.getHeader("Authorization").substring(7);
            String email = jwtUtil.extractUsername(token);

            return ResponseEntity.ok(
                    userApplicationService.getUserApp(email, appId)
            );

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}