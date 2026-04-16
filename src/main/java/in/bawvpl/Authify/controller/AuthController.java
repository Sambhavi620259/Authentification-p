package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.entity.KycEntity;
import in.bawvpl.Authify.repository.KycRepository;
import in.bawvpl.Authify.repository.UserRepository;
import in.bawvpl.Authify.io.AuthResponse;
import in.bawvpl.Authify.io.RegisterRequest;
import in.bawvpl.Authify.service.AppUserDetailsService;
import in.bawvpl.Authify.service.RegisterService;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.time.Instant;
import java.util.*;

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

    // ================= REGISTER =================
    @PostMapping("/register")
    public ResponseEntity<?> register(
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
            // ================= VALIDATION =================
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "File is required"));
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of("message", "File size must be < 5MB"));
            }

            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
            }

            if (password == null || password.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Password is required"));
            }

            // ================= FILE TYPE =================
            String contentType = file.getContentType();
            if (contentType == null ||
                    (!contentType.startsWith("image/") && !contentType.equals("application/pdf"))) {
                return ResponseEntity.badRequest().body(Map.of("message", "Only image or PDF allowed"));
            }

            // ================= SAVE FILE =================
            Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads");

            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadDir.resolve(fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // ================= NORMALIZE =================
            email = email.toLowerCase().trim();

            // ================= PREPARE REQUEST =================
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

            // ================= REGISTER USER =================
            UserEntity user = registerService.registerUser(req);

            // ================= SAVE KYC =================
            KycEntity kyc = KycEntity.builder()
                    .user(user)
                    .documentType(documentType)
                    .documentNumber(documentNumber)
                    .filePath(fileName)
                    .status("PENDING")
                    .completed(false)
                    .uploadedAt(Instant.now())
                    .build();

            kycRepository.save(kyc);

            log.info("User registered successfully: {}", email);

            return ResponseEntity.ok(Map.of(
                    "message", "Registered successfully. Please verify your email.",
                    "userId", user.getUserId(),
                    "referralCode", user.getReferralCode()
            ));

        } catch (Exception e) {
            log.error("Registration failed", e);

            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Registration failed: " + e.getMessage()));
        }
    }

    // ================= EMAIL VERIFY =================
    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {

        try {
            UserEntity user = userRepository.findByVerificationToken(token)
                    .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

            user.setEmailVerified(true);
            user.setVerificationToken(null);

            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "message", "Email verified successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage()
            ));
        }
    }

    // ================= LOGIN =================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        try {
            String email = request.getEmail().toLowerCase().trim();

            UserEntity user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!Boolean.TRUE.equals(user.getEmailVerified())) {
                return ResponseEntity.status(403)
                        .body(Map.of("message", "Please verify your email first"));
            }

            appUserDetailsService.loginAndSendOtp(email, request.getPassword());

            return ResponseEntity.ok(Map.of(
                    "message", "OTP sent successfully"
            ));

        } catch (Exception e) {
            log.error("Login failed", e);

            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ================= VERIFY OTP =================
    @PostMapping("/login/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest request) {

        try {
            AuthResponse response = appUserDetailsService.verifyLoginOtp(
                    request.getEmail().toLowerCase().trim(),
                    request.getOtp()
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "token", response.getAccessToken(),
                    "userId", response.getProfile().getUserId()
            ));

        } catch (Exception e) {
            log.error("OTP verification failed", e);

            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
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
    }
}