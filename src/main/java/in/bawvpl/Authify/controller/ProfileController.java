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

    // ================= GET PROFILE =================
    @GetMapping
    public ProfileResponse getProfile(Authentication authentication) {

        String email = authentication.getName();

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

        return ProfileResponse.builder()
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

                // ✅ IMAGE
                .photoUrl(user.getPhotoUrl())

                .build();
    }

    // ================= UPLOAD PHOTO =================
    @PostMapping("/upload-photo")
    public ResponseEntity<?> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request
    ) {
        try {
            System.out.println("🔥 UPLOAD API CALLED");

            // ✅ CHECK FILE
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            System.out.println("📁 File name: " + file.getOriginalFilename());

            // ✅ GET TOKEN
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("Invalid token");
            }

            String token = authHeader.substring(7);
            String email = jwtUtil.extractUsername(token);

            System.out.println("👤 User email: " + email);

            // ✅ GET USER
            UserEntity user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // ✅ SAVE FILE (ABSOLUTE PATH FIX)
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);

            Files.copy(file.getInputStream(), filePath,
                    StandardCopyOption.REPLACE_EXISTING);

            System.out.println("✅ File saved at: " + filePath);

            // ✅ SAVE IN DB
            user.setPhotoUrl(fileName);
            userRepository.save(user);

            System.out.println("✅ DB UPDATED WITH PHOTO");

            return ResponseEntity.ok("Photo uploaded successfully");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }
}