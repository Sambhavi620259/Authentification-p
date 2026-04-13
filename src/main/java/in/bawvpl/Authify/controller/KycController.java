package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.KycEntity;
import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.repository.KycRepository;
import in.bawvpl.Authify.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1.0/kyc")
@RequiredArgsConstructor
@CrossOrigin("*")
public class KycController {

    private final KycRepository kycRepository;
    private final UserRepository userRepository;

    // ================= GET ALL KYC =================
    @GetMapping("/all")
    public ResponseEntity<?> getAllKyc() {
        List<KycEntity> list = kycRepository.findAll();
        return ResponseEntity.ok(list);
    }

    // ================= GET PENDING KYC =================
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingKyc() {
        List<KycEntity> list = kycRepository.findByStatus("PENDING");
        return ResponseEntity.ok(list);
    }

    // ================= GET USER KYC =================
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserKyc(@PathVariable String userId) {
        try {

            UserEntity user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            KycEntity kyc = kycRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("KYC not found"));

            return ResponseEntity.ok(kyc);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }

    // ================= VERIFY KYC =================
    @PutMapping("/verify/{userId}")
    public ResponseEntity<?> verifyKyc(@PathVariable String userId) {
        try {

            UserEntity user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            KycEntity kyc = kycRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("KYC not found"));

            kyc.setStatus("VERIFIED");
            kyc.setCompleted(true);

            kycRepository.save(kyc);

            return ResponseEntity.ok(
                    Map.of(
                            "message", "KYC VERIFIED",
                            "userId", userId,
                            "status", "VERIFIED"
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }

    // ================= REJECT KYC =================
    @PutMapping("/reject/{userId}")
    public ResponseEntity<?> rejectKyc(@PathVariable String userId) {
        try {

            UserEntity user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            KycEntity kyc = kycRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("KYC not found"));

            kyc.setStatus("REJECTED");
            kyc.setCompleted(false);

            kycRepository.save(kyc);

            return ResponseEntity.ok(
                    Map.of(
                            "message", "KYC REJECTED",
                            "userId", userId,
                            "status", "REJECTED"
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }
}