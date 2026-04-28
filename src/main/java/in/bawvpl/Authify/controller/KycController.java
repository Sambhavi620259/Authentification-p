package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.KycEntity;
import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.io.ApiResponse;
import in.bawvpl.Authify.repository.KycRepository;
import in.bawvpl.Authify.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1.0/kyc")
@RequiredArgsConstructor
@Slf4j
public class KycController {

    private final KycRepository kycRepository;
    private final UserRepository userRepository;

    private static final String PENDING = "PENDING";
    private static final String VERIFIED = "VERIFIED";
    private static final String REJECTED = "REJECTED";

    private static final String UPLOAD_DIR = "uploads/kyc/";

    // ================= HELPER =================
    private String getEmail(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return auth.getName();
    }

    // ================= UPLOAD =================
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> upload(
            Authentication auth,
            @RequestParam String documentType,
            @RequestParam String documentNumber,
            @RequestParam MultipartFile file
    ) {

        String email = getEmail(auth);

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (kycRepository.existsByUser_Id(user.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "KYC already submitted");
        }

        try {
            // create directory
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            KycEntity kyc = KycEntity.builder()
                    .user(user)
                    .documentType(documentType)
                    .documentNumber(documentNumber)
                    .filePath(fileName)
                    .status(PENDING)
                    .completed(false)
                    .uploadedAt(Instant.now())
                    .build();

            kycRepository.save(kyc);

            return ok("KYC submitted", "SUCCESS");

        } catch (Exception e) {
            log.error("KYC upload failed", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Upload failed");
        }
    }

    // ================= GET MY KYC =================
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<KycEntity>> getMyKyc(Authentication auth) {

        String email = getEmail(auth);

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        KycEntity kyc = kycRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "KYC not found"));

        return ok("KYC fetched", kyc);
    }

    // ================= ADMIN =================

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<KycEntity>>> getAllKyc() {
        return ok("All KYC fetched", kycRepository.findAllByOrderByUploadedAtDesc());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<KycEntity>>> getPendingKyc() {
        return ok("Pending KYC fetched", kycRepository.findByStatusIgnoreCase(PENDING));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/verify/{userId}")
    public ResponseEntity<ApiResponse<String>> verifyKyc(@PathVariable Long userId) {

        UserEntity user = getUser(userId);
        KycEntity kyc = getKyc(user);

        kyc.setStatus(VERIFIED);
        kyc.setCompleted(true);
        kycRepository.save(kyc);

        user.setIsKycVerified(true);
        userRepository.save(user);

        return ok("KYC VERIFIED", VERIFIED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/reject/{userId}")
    public ResponseEntity<ApiResponse<String>> rejectKyc(@PathVariable Long userId) {

        UserEntity user = getUser(userId);
        KycEntity kyc = getKyc(user);

        kyc.setStatus(REJECTED);
        kyc.setCompleted(false);
        kycRepository.save(kyc);

        user.setIsKycVerified(false);
        userRepository.save(user);

        return ok("KYC REJECTED", REJECTED);
    }

    // ================= HELPERS =================

    private UserEntity getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private KycEntity getKyc(UserEntity user) {
        return kycRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "KYC not found"));
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(
                ApiResponse.<T>builder()
                        .status(200)
                        .message(message)
                        .data(data)
                        .build()
        );
    }
}