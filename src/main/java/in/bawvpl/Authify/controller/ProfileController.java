package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.io.ProfileResponse;
import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.entity.KycEntity;
import in.bawvpl.Authify.repository.UserRepository;
import in.bawvpl.Authify.repository.KycRepository;
import in.bawvpl.Authify.service.ProfileService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1.0/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final UserRepository userRepository;
    private final KycRepository kycRepository;
    private final ProfileService profileService;

    private static final String BASE_URL = "http://43.205.116.38:8080";

    // ================= GET PROFILE =================
    @GetMapping
    public ResponseEntity<?> getProfile(Authentication authentication) {

        String email = authentication.getName().toLowerCase().trim();

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<KycEntity> kycOpt = kycRepository.findByUser(user);

        String documentType = null;
        String documentNumber = null;
        String kycStatus = null;
        String filePath = null;
        String rejectionReason = null;
        boolean isKycVerified = false;

        if (kycOpt.isPresent()) {
            KycEntity kyc = kycOpt.get();

            documentType = kyc.getDocumentType();
            documentNumber = kyc.getDocumentNumber();
            kycStatus = kyc.getStatus();
            filePath = kyc.getFilePath();
            rejectionReason = kyc.getRejectionReason();

            isKycVerified = "VERIFIED".equalsIgnoreCase(kycStatus);
        }

        String photoUrl = null;
        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isBlank()) {
            photoUrl = BASE_URL + "/uploads/" + user.getPhotoUrl();
        }

        ProfileResponse response = ProfileResponse.builder()
                .userId(user.getUserId())
                .name(user.getEntityName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .isAccountVerified(Boolean.TRUE.equals(user.getEmailVerified()))
                .isKycVerified(isKycVerified)
                .referralCode(user.getReferralCode())
                .documentType(documentType)
                .documentNumber(documentNumber)
                .kycStatus(kycStatus)
                .filePath(filePath)
                .photoUrl(photoUrl)
                .build();

        return ResponseEntity.ok(Map.of(
                "profile", response,
                "kycRejectionReason", rejectionReason
        ));
    }

    // ================= UPDATE PROFILE =================
    @PutMapping
    public ResponseEntity<?> updateProfile(
            Authentication auth,
            @RequestBody Map<String, String> body
    ) {

        String email = auth.getName().toLowerCase().trim();

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (body.containsKey("name")) {
            user.setEntityName(body.get("name"));
        }

        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Profile updated"));
    }

    // ================= UPLOAD PHOTO =================
    @PostMapping("/upload-photo")
    public ResponseEntity<?> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {

        String email = authentication.getName().toLowerCase().trim();

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "File is empty"));
        }

        if (file.getSize() > 1 * 1024 * 1024) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "File size must be < 1MB"));
        }

        try {
            UserEntity user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            Path path = Paths.get("uploads");
            if (!Files.exists(path)) Files.createDirectories(path);

            Files.copy(file.getInputStream(), path.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);

            user.setPhotoUrl(fileName);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "photoUrl", BASE_URL + "/uploads/" + fileName
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Upload failed"));
        }
    }

    // ================= CHANGE EMAIL =================
    @PostMapping("/change-email")
    public ResponseEntity<?> changeEmail(Authentication auth,
                                         @RequestBody Map<String, String> req) {

        profileService.requestEmailChange(auth.getName(), req.get("newEmail"));
        return ResponseEntity.ok(Map.of("message", "Verification email sent"));
    }

    @GetMapping("/verify-email-change")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {

        profileService.verifyEmailChange(token);
        return ResponseEntity.ok(Map.of("message", "Email updated"));
    }

    // ================= PHONE OTP =================
    @PostMapping("/change-phone")
    public ResponseEntity<?> changePhone(Authentication auth,
                                         @RequestBody Map<String,String> req) {

        profileService.sendPhoneOtp(auth.getName(), req.get("phoneNumber"));
        return ResponseEntity.ok(Map.of("message", "OTP sent"));
    }

    @PostMapping("/verify-phone")
    public ResponseEntity<?> verifyPhone(Authentication auth,
                                         @RequestBody Map<String,String> req) {

        profileService.verifyPhoneOtp(auth.getName(), req.get("otp"));
        return ResponseEntity.ok(Map.of("message", "Phone verified"));
    }

    // ================= LAST LOGIN =================
    @GetMapping("/last-login")
    public ResponseEntity<?> lastLogin(Authentication auth) {

        return ResponseEntity.ok(Map.of(
                "lastLogin", profileService.getLastLogin(auth.getName())
        ));
    }

    // ================= KYC REJECTION =================
    @GetMapping("/kyc-reason")
    public ResponseEntity<?> kycReason(Authentication auth) {

        return ResponseEntity.ok(Map.of(
                "reason", profileService.getKycRejectionReason(auth.getName())
        ));
    }
}