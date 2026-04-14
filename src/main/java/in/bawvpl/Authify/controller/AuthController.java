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

            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "File is required"));
            }

            // ✅ FILE UPLOAD
            Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads");
            Files.createDirectories(uploadDir);

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadDir.resolve(fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // ✅ PREPARE REQUEST
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

            // ✅ REGISTER USER
            UserEntity user = registerService.registerUser(req);

            // ✅ SAVE KYC
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

            return ResponseEntity.ok(Map.of(
                    "message", "Registered successfully. Please verify your email.",
                    "userId", user.getUserId(),
                    "referralCode", user.getReferralCode()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ================= EMAIL VERIFY =================
    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {

        Optional<UserEntity> optionalUser =
                userRepository.findByVerificationToken(token);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Invalid or expired token"));
        }

        UserEntity user = optionalUser.get();

        user.setEmailVerified(true);
        user.setVerificationToken(null);

        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "Email verified successfully"
        ));
    }

    // ================= LOGIN =================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {

            System.out.println("LOGIN API CALLED: " + request.getEmail());

            Optional<UserEntity> userOpt =
                    userRepository.findByEmail(request.getEmail());

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(400)
                        .body(Map.of("message", "User not found"));
            }

            UserEntity user = userOpt.get();

            // ✅ BLOCK IF NOT VERIFIED
            if (!Boolean.TRUE.equals(user.getEmailVerified())) {
                return ResponseEntity.status(403)
                        .body(Map.of("message", "Please verify your email first"));
            }

            // ✅ SEND OTP
            appUserDetailsService.loginAndSendOtp(
                    request.getEmail(),
                    request.getPassword()
            );

            return ResponseEntity.ok(Map.of(
                    "message", "OTP sent successfully"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ================= VERIFY OTP =================
    @PostMapping("/login/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest request) {
        try {

            System.out.println("VERIFY OTP: " + request.getEmail() + " OTP: " + request.getOtp());

            AuthResponse response = appUserDetailsService.verifyLoginOtp(
                    request.getEmail(),
                    request.getOtp()
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "token", response.getAccessToken(),   // ✅ FIXED
                    "userId", response.getProfile().getUserId()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400)
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