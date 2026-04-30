package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.repository.UserRepository;
import in.bawvpl.Authify.service.*;

import in.bawvpl.Authify.util.JwtUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.Map;

@RestController
@RequestMapping("/api/v1.0/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin("*")
public class AuthController {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final JwtUtil jwtUtil;
    private final TwoFactorService twoFactorService;

    // ================= REQUEST OTP =================
    @PostMapping("/request-otp")
    public ResponseEntity<?> requestOtp(@RequestBody LoginRequest request) {

        try {
            String email = request.getEmail().toLowerCase().trim();

            UserEntity user = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!Boolean.TRUE.equals(user.getEmailVerified())) {
                throw new RuntimeException("Email not verified");
            }

            // 🔥 SAME OTP → email + phone
            otpService.generateLoginOtp(user);

            return ResponseEntity.ok(
                    Map.of("message", "OTP sent to email and phone")
            );

        } catch (Exception e) {
            log.error("❌ Request OTP error", e);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ================= VERIFY OTP =================
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest request) {

        try {

            String email = request.getEmail().toLowerCase().trim();

            UserEntity user = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // ✅ VERIFY SAME OTP
            otpService.verifyLoginOtp(user, request.getOtp());

            // 🔐 OPTIONAL 2FA
            if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {

                if (request.getTwoFactorCode() == null) {
                    throw new RuntimeException("2FA code required");
                }

                twoFactorService.validateLoginOtp(email, request.getTwoFactorCode());
            }

            // 🔥 GENERATE JWT
            String token = jwtUtil.generateToken(user.getEmail());

            return ResponseEntity.ok(
                    Map.of(
                            "message", "Login successful",
                            "token", token
                    )
            );

        } catch (Exception e) {

            log.error("❌ Verify OTP error", e);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ================= 2FA SETUP =================
    @PostMapping("/2fa/setup")
    public ResponseEntity<?> setup2FA(Authentication auth) {
        return ResponseEntity.ok(twoFactorService.generateSetup(auth.getName()));
    }

    @PostMapping("/2fa/verify")
    public ResponseEntity<?> verify2FA(Authentication auth,
                                       @RequestBody Map<String, String> req) {

        twoFactorService.verifyAndEnable(auth.getName(), req.get("code"));
        return ResponseEntity.ok(Map.of("message", "2FA enabled"));
    }

    // ================= DTO =================
    @Data
    static class LoginRequest {
        private String email;
    }

    @Data
    static class VerifyOtpRequest {
        private String email;
        private String otp;
        private String twoFactorCode;
    }
}