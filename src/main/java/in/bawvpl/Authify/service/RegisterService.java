package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.io.RegisterRequest;
import in.bawvpl.Authify.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserEntity registerUser(RegisterRequest req) {

        // ================= VALIDATION =================

        // ✅ CHECK EMAIL
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // ✅ CHECK PHONE
        if (userRepository.existsByPhoneNumber(req.getPhoneNumber())) {
            throw new RuntimeException("Phone number already registered");
        }

        // ================= ROLE =================
        String role = "ROLE_USER";

        if ("ADMIN".equalsIgnoreCase(req.getEntityType())) {
            role = "ROLE_ADMIN";
        }

        // ================= CREATE USER =================
        UserEntity user = UserEntity.builder()
                .userId("USR-" + UUID.randomUUID().toString().substring(0, 8)) // ✅ Better ID
                .entityType(req.getEntityType())
                .entityName(req.getName())
                .contactPerson(req.getName())
                .email(req.getEmail())
                .phoneNumber(req.getPhoneNumber())
                .password(passwordEncoder.encode(req.getPassword()))
                .adminRole(role)
                .address(req.getAddress()) // ✅ NEW
                .referralCode(req.getReferralCode()) // ✅ NEW
                .emailVerified(false)
                .userStatus("ACTIVE")
                .build();

        return userRepository.save(user);
    }
}