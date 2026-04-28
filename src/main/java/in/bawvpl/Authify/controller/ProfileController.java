package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.io.ProfileResponse;
import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.entity.KycEntity;
import in.bawvpl.Authify.repository.UserRepository;
import in.bawvpl.Authify.repository.KycRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
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
        boolean isKycVerified = false;

        if (kycOpt.isPresent()) {
            KycEntity kyc = kycOpt.get();

            documentType = kyc.getDocumentType();
            documentNumber = kyc.getDocumentNumber();
            kycStatus = kyc.getStatus();
            filePath = kyc.getFilePath();
            isKycVerified = "VERIFIED".equalsIgnoreCase(kyc.getStatus());
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

        return ResponseEntity.ok(response);
    }

    // ================= GET PROFILE (ALIAS FIX) =================
    // 👉 This solves your /profile/me error
    @GetMapping("/me")
    public ResponseEntity<?> getProfileAlias(Authentication auth) {
        return getProfile(auth);
    }

    // ================= UPDATE PROFILE =================
    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(
            Authentication auth,
            @RequestBody Map<String, String> req
    ) {

        String email = auth.getName().toLowerCase().trim();

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (req.containsKey("name")) {
            user.setEntityName(req.get("name"));
        }

        if (req.containsKey("phoneNumber")) {
            user.setPhoneNumber(req.get("phoneNumber"));
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

        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.equals("image/jpeg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/jpg"))) {

            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Only JPG/PNG allowed"));
        }

        try {
            UserEntity user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String fileName = System.currentTimeMillis() + "_" +
                    file.getOriginalFilename().replaceAll("\\s+", "_");

            Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            user.setPhotoUrl(fileName);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "message", "Photo uploaded successfully",
                    "photoUrl", BASE_URL + "/uploads/" + fileName
            ));

        } catch (Exception e) {
            log.error("Upload failed: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Upload failed"));
        }
    }
}