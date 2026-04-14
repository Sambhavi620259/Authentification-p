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

    public UserEntity registerUser(RegisterRequest req) {

        // ================= VALIDATION =================

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
        String referralCode = referralService.generateUniqueReferralCode();
        String verificationToken = UUID.randomUUID().toString();

        // ================= CREATE USER =================

        UserEntity user = UserEntity.builder()
                .userId(userId)
                .entityType(req.getEntityType())
                .entityName(req.getName())
                .contactPerson(req.getName())
                .email(req.getEmail())
                .phoneNumber(req.getPhoneNumber())
                .password(passwordEncoder.encode(req.getPassword()))
                .adminRole(role)
                .address(req.getAddress())
                .referralCode(referralCode) // ✅ OWN CODE
                .verificationToken(verificationToken) // ✅ EMAIL TOKEN
                .emailVerified(false)
                .userStatus("ACTIVE")
                .build();

        // ================= APPLY REFERRAL =================

        if (req.getReferralCode() != null && !req.getReferralCode().isBlank()) {

            Optional<UserEntity> refUser =
                    userRepository.findByReferralCode(req.getReferralCode().trim());

            if (refUser.isPresent()) {
                user.setReferredBy(req.getReferralCode().trim());
            }
        }

        return userRepository.save(user);
    }
}