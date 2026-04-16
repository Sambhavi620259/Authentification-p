package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.KycEntity;
import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.repository.KycRepository;
import in.bawvpl.Authify.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
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

    // ================= STATUS =================
    private static final String PENDING = "PENDING";
    private static final String VERIFIED = "VERIFIED";
    private static final String REJECTED = "REJECTED";

    // ================= GET ALL =================
    @GetMapping("/all")
    public ResponseEntity<?> getAllKyc() {

        List<KycEntity> list = kycRepository.findAll();

        return ResponseEntity.ok(Map.of(
                "message", "All KYC fetched",
                "count", list.size(),
                "data", list
        ));
    }

    // ================= GET PENDING =================
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingKyc() {

        // ✅ FIXED METHOD
        List<KycEntity> list = kycRepository.findByStatusIgnoreCase(PENDING);

        return ResponseEntity.ok(Map.of(
                "message", "Pending KYC fetched",
                "count", list.size(),
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
            log.error("GET KYC ERROR for {}: {}", userId, e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ================= VERIFY =================
    @PutMapping("/verify/{userId}")
    public ResponseEntity<?> verifyKyc(@PathVariable String userId) {

        try {
            log.info("VERIFY KYC CALLED for {}", userId);

            UserEntity user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            KycEntity kyc = kycRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("KYC not found"));

            // ✅ UPDATE KYC
            kyc.setStatus(VERIFIED);
            kyc.setCompleted(true);
            kycRepository.save(kyc);

            // ✅ UPDATE USER
            user.setIsKycVerified(true);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "message", "KYC VERIFIED",
                    "userId", userId,
                    "status", VERIFIED
            ));

        } catch (Exception e) {
            log.error("VERIFY ERROR for {}: {}", userId, e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ================= REJECT =================
    @PutMapping("/reject/{userId}")
    public ResponseEntity<?> rejectKyc(@PathVariable String userId) {

        try {
            log.info("REJECT KYC CALLED for {}", userId);

            UserEntity user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            KycEntity kyc = kycRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("KYC not found"));

            // ❌ UPDATE
            kyc.setStatus(REJECTED);
            kyc.setCompleted(false);
            kycRepository.save(kyc);

            // ❌ UPDATE USER
            user.setIsKycVerified(false);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "message", "KYC REJECTED",
                    "userId", userId,
                    "status", REJECTED
            ));

        } catch (Exception e) {
            log.error("REJECT ERROR for {}: {}", userId, e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}