package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.KycEntity;
import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.repository.KycRepository;
import in.bawvpl.Authify.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

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

    private static final String PENDING = "PENDING";
    private static final String VERIFIED = "VERIFIED";
    private static final String REJECTED = "REJECTED";

    // ================= COMMON RESPONSE =================
    private Map<String, Object> response(String message, Object data) {
        return Map.of(
                "message", message,
                "data", data
        );
    }

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

        KycEntity kyc = getKycOrThrow(userId);

        return ResponseEntity.ok(response("KYC fetched", kyc));
    }

    // ================= VERIFY =================
    @PutMapping("/verify/{userId}")
    public ResponseEntity<?> verifyKyc(@PathVariable String userId) {

        log.info("VERIFY KYC for {}", userId);

        UserEntity user = getUserOrThrow(userId);
        KycEntity kyc = getKycOrThrow(userId);

        kyc.setStatus(VERIFIED);
        kyc.setCompleted(true);
        kycRepository.save(kyc);

        user.setIsKycVerified(true);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "KYC VERIFIED",
                "userId", userId,
                "status", VERIFIED
        ));
    }

    // ================= REJECT =================
    @PutMapping("/reject/{userId}")
    public ResponseEntity<?> rejectKyc(@PathVariable String userId) {

        log.info("REJECT KYC for {}", userId);

        UserEntity user = getUserOrThrow(userId);
        KycEntity kyc = getKycOrThrow(userId);

        kyc.setStatus(REJECTED);
        kyc.setCompleted(false);
        kycRepository.save(kyc);

        user.setIsKycVerified(false);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "KYC REJECTED",
                "userId", userId,
                "status", REJECTED
        ));
    }

    // ================= HELPER METHODS =================

    private UserEntity getUserOrThrow(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private KycEntity getKycOrThrow(String userId) {

        UserEntity user = getUserOrThrow(userId);

        return kycRepository.findByUser(user)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "KYC not found"));
    }
}