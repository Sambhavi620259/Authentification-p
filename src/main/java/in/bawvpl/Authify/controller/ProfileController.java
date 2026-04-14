package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.io.ProfileResponse;
import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.entity.KycEntity;
import in.bawvpl.Authify.repository.UserRepository;
import in.bawvpl.Authify.repository.KycRepository;
import in.bawvpl.Authify.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1.0/profile")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ProfileController {

    private final UserRepository userRepository;
    private final KycRepository kycRepository;
    private final JwtUtil jwtUtil;

    private static final String BASE_URL = "http://43.205.116.38:8080";

    // ================= GET PROFILE =================
    @GetMapping
    public ResponseEntity<?> getProfile(Authentication authentication) {
        try {

            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            String email = authentication.getName();
            System.out.println("👤 PROFILE REQUEST FOR: " + email);

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

            // ✅ FIX: Always return FULL IMAGE URL
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
                    .photoUrl(photoUrl)   // ✅ FINAL FIX
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body("Profile fetch failed: " + e.getMessage());
        }
    }

    // ================= UPLOAD PHOTO =================
    @PostMapping("/upload-photo")
    public ResponseEntity<?> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request
    ) {
        try {

            System.out.println("🔥 UPLOAD API CALLED");

            // ✅ FILE CHECK
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            System.out.println("📁 File name: " + file.getOriginalFilename());

            // ✅ TOKEN CHECK
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("Invalid token");
            }

            String token = authHeader.substring(7);
            String email = jwtUtil.extractUsername(token);

            if (email == null) {
                return ResponseEntity.status(401).body("Invalid token user");
            }

            System.out.println("👤 User email: " + email);

            // ✅ FIND USER
            UserEntity user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // ✅ FILE NAME
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            // ✅ UPLOAD PATH
            Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("✅ File saved at: " + filePath);

            // ✅ SAVE IN DB
            user.setPhotoUrl(fileName);
            userRepository.save(user);

            System.out.println("✅ DB UPDATED WITH PHOTO: " + fileName);

            return ResponseEntity.ok(
                    "Photo uploaded successfully: " + BASE_URL + "/uploads/" + fileName
            );

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body("Upload failed: " + e.getMessage());
        }
    }
}