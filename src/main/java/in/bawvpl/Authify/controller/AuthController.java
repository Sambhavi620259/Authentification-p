package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.entity.KycEntity;
import in.bawvpl.Authify.repository.KycRepository;
import in.bawvpl.Authify.repository.UserRepository;
import in.bawvpl.Authify.io.AuthResponse;
import in.bawvpl.Authify.io.RegisterRequest;
import in.bawvpl.Authify.service.AppUserDetailsService;
import in.bawvpl.Authify.service.RegisterService;
import in.bawvpl.Authify.service.EmailService;

import lombok.Data;
import lombok.RequiredArgsConstructor;

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
public class AuthController {

    private final AppUserDetailsService appUserDetailsService;
    private final RegisterService registerService;
    private final EmailService emailService;
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

            if (documentType == null || documentType.isBlank()
                    || documentNumber == null || documentNumber.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Document details required"));
            }

            if ("AADHAAR".equalsIgnoreCase(documentType) && !documentNumber.matches("\\d{12}")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Aadhaar must be 12 digits"));
            }

            if ("PAN".equalsIgnoreCase(documentType)
                    && !documentNumber.matches("[A-Z]{5}[0-9]{4}[A-Z]{1}")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid PAN format"));
            }

            // ================= FILE UPLOAD =================

            Path uploadDir = Paths.get("uploads").toAbsolutePath();
            Files.createDirectories(uploadDir);

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadDir.resolve(fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // ================= PREPARE REQUEST =================

            RegisterRequest req = new RegisterRequest();
            req.setEntityType(entityType);
            req.setName(name);
            req.setEmail(email);
            req.setPhoneNumber(phoneNumber);
            req.setPassword(password);
            req.setAddress(address);
            req.setReferralCode(referralCode);

            // ================= REGISTER USER =================

            UserEntity user = registerService.registerUser(req);

            // ================= SEND EMAIL =================

            String verifyLink = "http://localhost:8080/api/v1.0/verify?token="
                    + user.getVerificationToken();

            emailService.sendVerificationEmail(user.getEmail(), verifyLink);

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

            // ================= RESPONSE =================

            return ResponseEntity.ok(Map.of(
                    "message", "Registered successfully. Please verify your email.",
                    "userId", user.getUserId(),
                    "referralCode", user.getReferralCode()
            ));

        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.internalServerError().body(
                    Map.of("message", e.getMessage())
            );
        }
    }

    // ================= EMAIL VERIFY =================
    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {

        Optional<UserEntity> optionalUser =
                userRepository.findByVerificationToken(token);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid or expired token");
        }

        UserEntity user = optionalUser.get();

        user.setEmailVerified(true);
        user.setVerificationToken(null);

        userRepository.save(user);

        return ResponseEntity.ok("Email verified successfully. You can now login.");
    }

    // ================= LOGIN =================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {

            Optional<UserEntity> userOpt =
                    userRepository.findByEmail(request.getEmail());

            if (userOpt.isPresent() && !userOpt.get().getEmailVerified()) {
                return ResponseEntity.status(403).body(
                        Map.of("message", "Please verify your email first")
                );
            }

            appUserDetailsService.loginAndSendOtp(
                    request.getEmail(),
                    request.getPassword()
            );

            return ResponseEntity.ok(Map.of("message", "OTP sent"));

        } catch (Exception e) {
            return ResponseEntity.status(400).body(
                    Map.of("message", e.getMessage())
            );
        }
    }

    // ================= VERIFY OTP =================
    @PostMapping("/login/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest request) {
        try {

            AuthResponse response = appUserDetailsService.verifyLoginOtp(
                    request.getEmail(),
                    request.getOtp()
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "token", response.getAccessToken(),
                    "user", response.getProfile()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(400).body(
                    Map.of("message", e.getMessage())
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