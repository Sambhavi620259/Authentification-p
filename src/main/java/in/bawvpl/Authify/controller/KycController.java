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

    // ================= GET ALL (ADMIN ONLY) =================
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<KycEntity>>> getAllKyc() {

        List<KycEntity> list = kycRepository.findAll();

        return ok("All KYC fetched", list);
    }

    // ================= GET PENDING (ADMIN ONLY) =================
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<KycEntity>>> getPendingKyc() {

        List<KycEntity> list = kycRepository.findByStatusIgnoreCase(PENDING);

        return ok("Pending KYC fetched", list);
    }

    // ================= GET MY KYC =================
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<KycEntity>> getMyKyc(Authentication auth) {

        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        String email = auth.getName();

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        KycEntity kyc = kycRepository.findByUser(user)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "KYC not found"));

        return ok("KYC fetched", kyc);
    }

    // ================= VERIFY (ADMIN ONLY) =================
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/verify/{userId}")
    public ResponseEntity<ApiResponse<String>> verifyKyc(@PathVariable String userId) {

        log.info("VERIFY KYC for {}", userId);

        UserEntity user = getUserOrThrow(userId);
        KycEntity kyc = getKycOrThrow(user);

        kyc.setStatus(VERIFIED);
        kyc.setCompleted(true);
        kycRepository.save(kyc);

        user.setIsKycVerified(true);
        userRepository.save(user);

        return ok("KYC VERIFIED", VERIFIED);
    }

    // ================= REJECT (ADMIN ONLY) =================
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/reject/{userId}")
    public ResponseEntity<ApiResponse<String>> rejectKyc(@PathVariable String userId) {

        log.info("REJECT KYC for {}", userId);

        UserEntity user = getUserOrThrow(userId);
        KycEntity kyc = getKycOrThrow(user);

        kyc.setStatus(REJECTED);
        kyc.setCompleted(false);
        kycRepository.save(kyc);

        user.setIsKycVerified(false);
        userRepository.save(user);

        return ok("KYC REJECTED", REJECTED);
    }

    // ================= HELPERS =================

    private UserEntity getUserOrThrow(String userId) {

        return userRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private KycEntity getKycOrThrow(UserEntity user) {

        return kycRepository.findByUser(user)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "KYC not found"));
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