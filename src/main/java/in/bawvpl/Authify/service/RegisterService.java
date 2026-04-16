package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.io.RegisterRequest;
import in.bawvpl.Authify.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReferralService referralService;
    private final EmailService emailService;

    @Transactional
    public UserEntity registerUser(RegisterRequest req) {

        // ================= NORMALIZE =================
        String email = req.getEmail().toLowerCase().trim();

        // ================= VALIDATION =================

        if (email.isBlank()) {
            throw new RuntimeException("Email is required");
        }

        if (req.getPhoneNumber() == null || req.getPhoneNumber().isBlank()) {
            throw new RuntimeException("Phone number is required");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        if (userRepository.existsByPhoneNumber(req.getPhoneNumber())) {
            throw new RuntimeException("Phone number already registered");
        }

        // ================= ROLE =================
        String role = "ROLE_USER";

        if ("ORGANIZATION".equalsIgnoreCase(req.getEntityType())) {
            role = "ROLE_ADMIN"; // 🔥 FIXED LOGIC
        }

        // ================= GENERATE VALUES =================

        String userId = "USR-" + UUID.randomUUID().toString().substring(0, 8);

        String referralCode = referralService.generateUniqueReferralCode();

        String verificationToken = UUID.randomUUID().toString();

        log.info("Generated verification token for {}", email);

        // ================= CREATE USER =================

        UserEntity user = UserEntity.builder()
                .userId(userId)
                .entityType(req.getEntityType())
                .entityName(req.getName())
                .contactPerson(req.getName())
                .email(email)
                .phoneNumber(req.getPhoneNumber())
                .password(passwordEncoder.encode(req.getPassword()))
                .adminRole(role) // 🔥 FIXED FIELD
                .address(req.getAddress())

                // REFERRAL
                .referralCode(referralCode)

                // EMAIL VERIFY
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
                log.warn("Invalid referral code: {}", refCode);
            }
        }

        // ================= SAVE USER =================

        UserEntity savedUser = userRepository.save(user);

        if (savedUser.getVerificationToken() == null) {
            throw new RuntimeException("Verification token not saved!");
        }

        // ================= SEND EMAIL =================

        String verificationLink =
                "http://43.205.116.38:8080/api/v1.0/verify?token=" + savedUser.getVerificationToken();

        emailService.sendVerificationEmail(savedUser.getEmail(), verificationLink);

        log.info("Verification email sent to {}", savedUser.getEmail());

        return savedUser;
    }
}