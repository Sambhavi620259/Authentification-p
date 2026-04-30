package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.entity.KycEntity;
import in.bawvpl.Authify.repository.KycRepository;
import in.bawvpl.Authify.repository.UserRepository;
import in.bawvpl.Authify.io.*;
import in.bawvpl.Authify.service.*;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;

import java.nio.file.*;
import java.time.Instant;
import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("/api/v1.0")
@RequiredArgsConstructor
@CrossOrigin("*")
@Slf4j
public class AuthController {

    private final AppUserDetailsService appUserDetailsService;
    private final RegisterService registerService;
    private final KycRepository kycRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    // 🔥 NEW
    private final TwoFactorService twoFactorService;

    // ================= REGISTER =================
    @PostMapping(value = "/register", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<Object>> register(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType,
            @RequestParam("documentNumber") String documentNumber,
            @RequestParam(value = "referralCode", required = false) String referralCode,
            @RequestParam("entityType") String entityType,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("password") String password,
            @RequestParam("address") String address
    ) {

        try {

            if (file == null || file.isEmpty()) {
                throw new RuntimeException("File is required");
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                throw new RuntimeException("File size must be < 5MB");
            }

            Path uploadDir = Paths.get("uploads");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadDir.resolve(fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            email = email.toLowerCase().trim();

            RegisterRequest req = new RegisterRequest();
            req.setEntityType(entityType);
            req.setName(name);
            req.setEmail(email);
            req.setPhoneNumber(phoneNumber);
            req.setPassword(password);
            req.setAddress(address);
            req.setReferralCode(referralCode);
            req.setDocumentType(documentType);
            req.setDocumentNumber(documentNumber);

            UserEntity user = registerService.registerUser(req);

            kycRepository.save(KycEntity.builder()
                    .user(user)
                    .documentType(documentType)
                    .documentNumber(documentNumber)
                    .filePath(fileName)
                    .status("PENDING")
                    .completed(false)
                    .uploadedAt(Instant.now())
                    .build());

            return ResponseEntity.ok(ApiResponse.builder()
                    .status(200)
                    .message("Registered successfully. Please verify your email.")
                    .data(user.getUserId())
                    .build());

        } catch (Exception e) {

            log.error("❌ Register error: ", e);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.builder()
                            .status(400)
                            .message(e.getMessage())
                            .build());
        }
    }

    // ================= LOGIN =================
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Object>> login(@RequestBody LoginRequest request) {

        try {

            String email = request.getEmail().toLowerCase().trim();

            UserEntity user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!Boolean.TRUE.equals(user.getEmailVerified())) {
                throw new RuntimeException("Please verify email first");
            }

            appUserDetailsService.loginAndSendOtp(email, request.getPassword());

            return ResponseEntity.ok(ApiResponse.builder()
                    .status(200)
                    .message("OTP sent successfully")
                    .build());

        } catch (Exception e) {

            log.error("❌ Login error: ", e);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.builder()
                            .status(400)
                            .message(e.getMessage())
                            .build());
        }
    }

    // ================= VERIFY OTP + 2FA =================
    @PostMapping("/login/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@RequestBody VerifyOtpRequest request) {

        try {

            String email = request.getEmail().toLowerCase().trim();

            UserEntity user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // ✅ VERIFY LOGIN OTP
            AuthResponse response = appUserDetailsService.verifyLoginOtp(user, request.getOtp());

            // 🔥 ADD 2FA CHECK HERE
            if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {

                if (request.getTwoFactorCode() == null) {
                    throw new RuntimeException("2FA code required");
                }

                twoFactorService.validateLoginOtp(email, request.getTwoFactorCode());
            }

            return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                    .status(200)
                    .message("Login successful")
                    .data(response)
                    .build());

        } catch (Exception e) {

            log.error("❌ OTP verification error: ", e);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<AuthResponse>builder()
                            .status(400)
                            .message(e.getMessage())
                            .build());
        }
    }

    // ================= 2FA =================
    @PostMapping("/2fa/setup")
    public ResponseEntity<?> setup2FA(Authentication auth) {
        return ResponseEntity.ok(twoFactorService.generateSetup(auth.getName()));
    }

    @PostMapping("/2fa/verify")
    public ResponseEntity<?> verify2FA(Authentication auth,
                                       @RequestBody Map<String,String> req) {

        twoFactorService.verifyAndEnable(auth.getName(), req.get("code"));
        return ResponseEntity.ok("2FA enabled");
    }

    // ================= DTO =================
    @Data
    static class LoginRequest {
        private String email;
        private String password;
    }

    @Data
    static class VerifyOtpRequest {
        private String email;
        private String otp;

        // 🔥 ADD THIS
        private String twoFactorCode;
    }
}