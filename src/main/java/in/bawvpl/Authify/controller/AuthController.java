package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.io.AuthResponse;
import in.bawvpl.Authify.io.ProfileResponse;
import in.bawvpl.Authify.io.RegisterRequest;
import in.bawvpl.Authify.service.AppUserDetailsService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1.0")
@RequiredArgsConstructor
public class AuthController {

    private final AppUserDetailsService appUserDetailsService;

    // ✅ REGISTER
    @PostMapping("/register")
    public ResponseEntity<ProfileResponse> register(
            @Valid @RequestBody RegisterRequest registerRequest) {

        ProfileResponse response = appUserDetailsService.registerUser(registerRequest);
        return ResponseEntity.ok(response);
    }

    // ✅ LOGIN STEP 1 (GENERATE OTP)
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody LoginRequest request) {

        boolean otpSent = appUserDetailsService.loginAndSendOtp(
                request.getEmail(),
                request.getPassword()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("message", otpSent ? "OTP sent successfully" : "OTP sending failed");
        response.put("otpSent", otpSent);

        return ResponseEntity.ok(response);
    }

    // ✅ LOGIN STEP 2 (VERIFY OTP)
    @PostMapping("/login/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {

        AuthResponse response = appUserDetailsService.verifyLoginOtp(
                request.getEmail(),
                request.getOtp()
        );

        return ResponseEntity.ok(response);
    }

    // ================= DTOs =================

    @Data
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Data
    public static class VerifyOtpRequest {
        private String email;
        private String otp;
    }
}