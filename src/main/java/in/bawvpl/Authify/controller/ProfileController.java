package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.io.ProfileResponse;
import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.entity.KycEntity;
import in.bawvpl.Authify.repository.UserRepository;
import in.bawvpl.Authify.repository.KycRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1.0/profile")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ProfileController {

    private final UserRepository userRepository;
    private final KycRepository kycRepository;

    @GetMapping
    public ProfileResponse getProfile(Authentication authentication) {

        String email = authentication.getName();

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ================= GET KYC =================
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

        // ================= RESPONSE =================
        return ProfileResponse.builder()
                .userId(user.getUserId())
                .name(user.getEntityName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())

                .isAccountVerified(Boolean.TRUE.equals(user.getEmailVerified()))
                .isKycVerified(isKycVerified)

                // ✅ NEW FIELDS (IMPORTANT)
                .referralCode(user.getReferralCode())
                .documentType(documentType)
                .documentNumber(documentNumber)
                .kycStatus(kycStatus)
                .filePath(filePath)
                .photoUrl(user.getPhotoUrl())

                .build();
    }
}