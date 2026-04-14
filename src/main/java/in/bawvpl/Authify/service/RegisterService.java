package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.io.RegisterRequest;
import in.bawvpl.Authify.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReferralService referralService;
    private final EmailService emailService;

    public UserEntity registerUser(RegisterRequest req) {

        // ================= VALIDATION =================

        if (req.getEmail() == null || req.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }

        if (req.getPhoneNumber() == null || req.getPhoneNumber().isBlank()) {
            throw new RuntimeException("Phone number is required");
        }

        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        if (userRepository.existsByPhoneNumber(req.getPhoneNumber())) {
            throw new RuntimeException("Phone number already registered");
        }

        // ================= ROLE =================
        String role = "ROLE_USER";

        if ("ADMIN".equalsIgnoreCase(req.getEntityType())) {
            role = "ROLE_ADMIN";
        }

        // ================= GENERATE VALUES =================

        String userId = "USR-" + UUID.randomUUID().toString().substring(0, 8);

        // ✅ Generate unique referral code
        String referralCode = referralService.generateUniqueReferralCode();

        // ✅ Generate verification token
        String verificationToken = UUID.randomUUID().toString();

        System.out.println("Generated verification token: " + verificationToken);

        // ================= CREATE USER =================

        UserEntity user = UserEntity.builder()
                .userId(userId)
                .entityType(req.getEntityType())
                .entityName(req.getName())
                .contactPerson(req.getName())
                .email(req.getEmail().trim().toLowerCase())
                .phoneNumber(req.getPhoneNumber())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(role)
                .address(req.getAddress())

                // ✅ REFERRAL
                .referralCode(referralCode)

                // ✅ EMAIL VERIFICATION
                .verificationToken(verificationToken)
                .emailVerified(false)

                .userStatus("ACTIVE")
                .build();

        // ================= APPLY REFERRAL =================

        if (req.getReferralCode() != null && !req.getReferralCode().isBlank()) {

            String refCode = req.getReferralCode().trim();

            Optional<UserEntity> refUser =
                    userRepository.findByReferralCode(refCode);

            if (refUser.isPresent()) {
                user.setReferredBy(refCode);
            } else {
                System.out.println("⚠️ Invalid referral code: " + refCode);
            }
        }

        // ================= SAVE USER =================

        UserEntity savedUser = userRepository.save(user);

        // ================= SAFETY CHECK =================

        if (savedUser.getVerificationToken() == null) {
            throw new RuntimeException("Verification token not saved!");
        }

        // ================= SEND EMAIL =================

        String verificationLink =
                "http://43.205.116.38:8080/api/v1.0/verify?token=" + savedUser.getVerificationToken();

        emailService.sendVerificationEmail(savedUser.getEmail(), verificationLink);

        System.out.println("✅ Verification email sent to: " + savedUser.getEmail());

        return savedUser;
    }
}