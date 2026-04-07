package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.io.AuthResponse;
import in.bawvpl.Authify.io.ProfileResponse;
import in.bawvpl.Authify.io.RegisterRequest;
import in.bawvpl.Authify.repository.UserRepository;
import in.bawvpl.Authify.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final OtpService otpService;
    private final JwtUtil jwtUtil;

    // ===============================
    // Spring Security Authentication
    // ===============================
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        UserEntity user = userRepository.findByEmail(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found: " + username));

        String role = user.getAdminRole();
        if (role == null || role.isBlank()) {
            role = "ROLE_USER";
        }

        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority(role)))
                .build();
    }

    // ===============================
    // REGISTER (TABLE-1 BASED)
    // ===============================
    @Transactional
    public ProfileResponse registerUser(@Valid RegisterRequest req) {

        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Email already registered");
        }

        if (userRepository.existsByMobile(req.getPhoneNumber())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Mobile already registered");
        }

        UserEntity user = UserEntity.builder()
                .userId(UUID.randomUUID().toString())

                // ✅ TABLE-1 FIELDS
                .entityType(req.getEntityType())
                .entityName(req.getName())
                .contactPerson(req.getName())
                .email(req.getEmail())
                .mobile(req.getPhoneNumber())
                .password(passwordEncoder.encode(req.getPassword()))

                // ✅ DEFAULT VALUES
                .adminRole("ROLE_USER")
                .userStatus("Active")
                .emailVerified(false)
                .passDate(LocalDateTime.now())
                .passStatus("Active")

                // ✅ REFERRAL DEFAULT
                .referredBy(1000000L)

                .build();

        userRepository.save(user);

        // 🔥 OPTIONAL: SEND EMAIL OTP
        try {
            String otp = otpService.generateRegisterOtp(user);
            emailService.sendVerificationOtpEmail(user.getEmail(), otp);
            log.info("REGISTER OTP SENT: {}", otp);
        } catch (Exception ex) {
            log.error("OTP email failed", ex);
        }

        return mapToProfile(user);
    }

    // ===============================
    // LOGIN STEP 1 → PASSWORD + OTP
    // ===============================
    public boolean loginAndSendOtp(String email, String password) {

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        if (!"Active".equalsIgnoreCase(user.getUserStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "User account is not active");
        }

        // 🔥 GENERATE OTP
        String otp = otpService.generateLoginOtp(user);

        log.info("LOGIN OTP GENERATED: {}", otp);

        try {
            emailService.sendVerificationOtpEmail(user.getEmail(), otp);
        } catch (Exception ex) {
            log.error("Email sending failed", ex);
        }

        return true;
    }

    // ===============================
    // LOGIN STEP 2 → VERIFY OTP + JWT
    // ===============================
    @Transactional
    public AuthResponse verifyLoginOtp(String email, String otp) {

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "User not found"));

        otpService.verifyLoginOtp(user, otp);

        String accessToken = jwtUtil.generateAccessToken(user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .profile(mapToProfile(user))
                .build();
    }

    // ===============================
    // HELPER: MAP PROFILE
    // ===============================
    private ProfileResponse mapToProfile(UserEntity user) {

        return ProfileResponse.builder()
                .userId(user.getUserId())
                .name(user.getEntityName())
                .email(user.getEmail())
                .phoneNumber(user.getMobile())
                .isAccountVerified(Boolean.TRUE.equals(user.getEmailVerified()))
                .isKycVerified(user.getKyc() != null)
                .build();
    }
}