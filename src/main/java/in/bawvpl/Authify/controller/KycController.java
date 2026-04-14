package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.KycEntity;
import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.repository.KycRepository;
import in.bawvpl.Authify.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1.0/kyc")
@RequiredArgsConstructor
@CrossOrigin("*")
@Slf4j
public class KycController {

    private final KycRepository kycRepository;
    private final UserRepository userRepository;

    // ================= GET ALL KYC =================
    @GetMapping("/all")
    public ResponseEntity<?> getAllKyc() {
        List<KycEntity> list = kycRepository.findAll();
        return ResponseEntity.ok(Map.of(
                "message", "All KYC fetched",
                "data", list
        ));
    }

    // ================= GET PENDING KYC =================
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingKyc() {
        List<KycEntity> list = kycRepository.findByStatus("PENDING");
        return ResponseEntity.ok(Map.of(
                "message", "Pending KYC fetched",
                "data", list
        ));
    }

    // ================= GET USER KYC =================
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserKyc(@PathVariable String userId) {
        try {
            UserEntity user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            KycEntity kyc = kycRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("KYC not found"));

            return ResponseEntity.ok(Map.of(
                    "message", "KYC fetched",
                    "data", kyc
            ));

        } catch (Exception e) {
            log.error("GET KYC ERROR: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }

    // ================= VERIFY KYC =================
    @PutMapping("/verify/{userId}")
    public ResponseEntity<?> verifyKyc(@PathVariable String userId) {
        try {
            log.info("VERIFY KYC CALLED for {}", userId);

            UserEntity user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            KycEntity kyc = kycRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("KYC not found"));

            // ✅ UPDATE KYC
            kyc.setStatus("VERIFIED");
            kyc.setCompleted(true);
            kycRepository.save(kyc);

            // ✅ ALSO UPDATE USER
            user.setIsKycVerified(true);
            userRepository.save(user);

            log.info("KYC VERIFIED for {}", userId);

            return ResponseEntity.ok(Map.of(
                    "message", "KYC VERIFIED SUCCESSFULLY",
                    "userId", userId,
                    "status", "VERIFIED"
            ));

        } catch (Exception e) {
            log.error("VERIFY ERROR: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }

    // ================= REJECT KYC =================
    @PutMapping("/reject/{userId}")
    public ResponseEntity<?> rejectKyc(@PathVariable String userId) {
        try {
            log.info("REJECT KYC CALLED for {}", userId);

            UserEntity user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            KycEntity kyc = kycRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("KYC not found"));

            // ❌ UPDATE
            kyc.setStatus("REJECTED");
            kyc.setCompleted(false);
            kycRepository.save(kyc);

            // ❌ UPDATE USER ALSO
            user.setIsKycVerified(false);
            userRepository.save(user);

            log.info("KYC REJECTED for {}", userId);

            return ResponseEntity.ok(Map.of(
                    "message", "KYC REJECTED",
                    "userId", userId,
                    "status", "REJECTED"
            ));

        } catch (Exception e) {
            log.error("REJECT ERROR: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }
}