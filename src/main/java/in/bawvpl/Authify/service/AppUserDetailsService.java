package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.entity.KycEntity;
import in.bawvpl.Authify.io.AuthResponse;
import in.bawvpl.Authify.io.ProfileResponse;
import in.bawvpl.Authify.repository.UserRepository;
import in.bawvpl.Authify.repository.KycRepository;
import in.bawvpl.Authify.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final OtpService otpService;
    private final JwtUtil jwtUtil;
    private final KycRepository kycRepository;

    // ================= LOAD USER =================
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        UserEntity user = userRepository.findByEmail(username.toLowerCase())
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        // 🔥 FIXED ROLE
        String role = (user.getRole() != null && !user.getRole().isBlank())
                ? user.getRole()
                : "ROLE_USER";

        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority(role)))
                .build();
    }

    // ================= LOGIN STEP 1 =================
    public void loginAndSendOtp(String email, String password) {

        email = email.trim().toLowerCase();

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Please verify your email first");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String otp = otpService.generateLoginOtp(user);

        log.info("LOGIN OTP GENERATED for {}", email);

        try {
            emailService.sendVerificationOtpEmail(email, otp);
        } catch (Exception ex) {
            log.error("Login OTP email failed", ex);
        }
    }

    // ================= LOGIN STEP 2 =================
    @Transactional
    public AuthResponse verifyLoginOtp(String email, String otp) {

        email = email.trim().toLowerCase();

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (otp == null || otp.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP required");
        }

        otpService.verifyLoginOtp(user, otp);

        String token = jwtUtil.generateAccessToken(user.getEmail());

        return AuthResponse.builder()
                .accessToken(token)
                .profile(mapToProfile(user))
                .build();
    }

    // ================= PROFILE =================
    private ProfileResponse mapToProfile(UserEntity user) {

        Optional<KycEntity> kycOpt = kycRepository.findByUser(user);

        boolean isKycVerified = false;
        String documentType = null;
        String documentNumber = null;
        String kycStatus = null;
        String filePath = null;

        if (kycOpt.isPresent()) {
            KycEntity kyc = kycOpt.get();

            documentType = kyc.getDocumentType();
            documentNumber = kyc.getDocumentNumber();
            kycStatus = kyc.getStatus();
            filePath = kyc.getFilePath();

            isKycVerified = "VERIFIED".equalsIgnoreCase(kyc.getStatus());
        }

        return ProfileResponse.builder()
                .userId(user.getUserId())
                .name(user.getEntityName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .isAccountVerified(Boolean.TRUE.equals(user.getEmailVerified()))
                .isKycVerified(isKycVerified)
                .referralCode(user.getReferralCode())
                .documentType(documentType)
                .documentNumber(documentNumber)
                .kycStatus(kycStatus)
                .filePath(filePath)
                .photoUrl(user.getPhotoUrl())
                .build();
    }
}