package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.entity.KycEntity;
import in.bawvpl.Authify.repository.KycRepository;
import in.bawvpl.Authify.repository.UserRepository;
import in.bawvpl.Authify.io.*;
import in.bawvpl.Authify.service.AppUserDetailsService;
import in.bawvpl.Authify.service.RegisterService;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.time.Instant;
import java.util.UUID;

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

            String contentType = file.getContentType();
            if (contentType == null ||
                    (!contentType.startsWith("image/") && !contentType.equals("application/pdf"))) {
                throw new RuntimeException("Only image or PDF allowed");
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

            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .status(200)
                            .message("Registered successfully. Please verify your email.")
                            .data(user.getUserId())
                            .build()
            );

        } catch (Exception e) {

            log.error("❌ Register error: ", e);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.builder()
                            .status(400)
                            .message(e.getMessage())
                            .data(null)
                            .build()
            );
        }
    }

    // ================= EMAIL VERIFY =================
    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<Object>> verifyEmail(@RequestParam String token) {

        try {

            UserEntity user = userRepository.findByVerificationToken(token)
                    .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

            user.setEmailVerified(true);
            user.setVerificationToken(null);

            userRepository.save(user);

            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .status(200)
                            .message("Email verified successfully")
                            .data(null)
                            .build()
            );

        } catch (Exception e) {

            log.error("❌ Email verification error: ", e);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.builder()
                            .status(400)
                            .message(e.getMessage())
                            .data(null)
                            .build()
            );
        }
    }

    // ================= LOGIN =================
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Object>> login(@RequestBody LoginRequest request) {

        try {

            if (request.getEmail() == null || request.getPassword() == null) {
                throw new RuntimeException("Email and password required");
            }

            String email = request.getEmail().toLowerCase().trim();

            UserEntity user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!Boolean.TRUE.equals(user.getEmailVerified())) {
                throw new RuntimeException("Please verify email first");
            }

            appUserDetailsService.loginAndSendOtp(email, request.getPassword());

            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .status(200)
                            .message("OTP sent successfully")
                            .data(null)
                            .build()
            );

        } catch (Exception e) {

            log.error("❌ Login error: ", e);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.builder()
                            .status(400)
                            .message(e.getMessage())
                            .data(null)
                            .build()
            );
        }
    }

    // ================= VERIFY OTP =================
    @PostMapping("/login/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@RequestBody VerifyOtpRequest request) {

        try {

            if (request.getEmail() == null || request.getOtp() == null) {
                throw new RuntimeException("Email and OTP required");
            }

            String email = request.getEmail().toLowerCase().trim();

            // ✅ FIX: get user first
            UserEntity user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // ✅ FIX: pass user instead of email
            AuthResponse response = appUserDetailsService.verifyLoginOtp(
                    user,
                    request.getOtp()
            );

            if (response == null || response.getToken() == null) {
                throw new RuntimeException("Token not generated");
            }

            return ResponseEntity.ok(
                    ApiResponse.<AuthResponse>builder()
                            .status(200)
                            .message("Login successful")
                            .data(response)
                            .build()
            );

        } catch (Exception e) {

            log.error("❌ OTP verification error: ", e);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<AuthResponse>builder()
                            .status(400)
                            .message(e.getMessage())
                            .data(null)
                            .build()
            );
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