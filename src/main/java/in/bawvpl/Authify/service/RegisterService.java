package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.KycEntity;
import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.io.RegisterRequest;
import in.bawvpl.Authify.repository.KycRepository;
import in.bawvpl.Authify.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KycRepository kycRepository;

    public UserEntity registerUser(RegisterRequest req) {

        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // ✅ FIX
        if (userRepository.existsByMobile(req.getPhoneNumber())) {
            throw new RuntimeException("Phone number already registered");
        }

        // ✅ FIXED USER ENTITY
        UserEntity user = UserEntity.builder()
                .userId(UUID.randomUUID().toString())
                .entityType(req.getEntityType())
                .entityName(req.getName())
                .contactPerson(req.getName())
                .email(req.getEmail())
                .mobile(req.getPhoneNumber())
                .password(passwordEncoder.encode(req.getPassword()))
                .adminRole("ROLE_USER")
                .emailVerified(false)
                .build();

        user = userRepository.save(user);

        // ✅ KYC
        KycEntity kyc = KycEntity.builder()
                .aadhaarNumber(req.getAadhaarNumber())
                .panNumber(req.getPanNumber())
                .status("VERIFIED")
                .completed(true)
                .uploadedAt(Instant.now())
                .user(user)
                .build();

        kycRepository.save(kyc);

        return user;
    }
}