package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.KycEntity;
import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.io.ProfileRequest;
import in.bawvpl.Authify.io.ProfileResponse;
import in.bawvpl.Authify.repository.KycRepository;
import in.bawvpl.Authify.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final KycRepository kycRepository;
    private final OtpService otpService;
    private final SmsService smsService;

    private static final String BASE_URL = "http://43.205.116.38:8080";

    // ================= REGISTER =================
    @Override
    @Transactional
    public ProfileResponse createProfile(ProfileRequest request) {

        String email = request.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        UserEntity user = UserEntity.builder()
                .userId("USR-" + UUID.randomUUID().toString().substring(0, 8))
                .entityName(request.getName())
                .email(email)
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("ROLE_USER")
                .address(request.getAddress())
                .emailVerified(false)
                .build();

        user = userRepository.save(user);

        String otp = otpService.generateRegisterOtp(user);
        emailService.sendVerificationOtpEmail(user.getEmail(), otp);

        return convertToProfileResponse(user);
    }

    // ================= OTP =================
    @Override
    public String verifyOtp(String email, String otp) {
        UserEntity user = findByEmailOrThrow(email);
        otpService.verifyRegisterOtp(user, otp);
        user.setEmailVerified(true);
        userRepository.save(user);
        return "Email verified";
    }

    @Override
    public void sendVerificationOtp(String email) {
        UserEntity user = findByEmailOrThrow(email);
        String otp = otpService.generateRegisterOtp(user);
        emailService.sendVerificationOtpEmail(email, otp);
    }

    @Override
    public void sendResetOtp(String email) {
        UserEntity user = findByEmailOrThrow(email);
        String otp = otpService.generateRegisterOtp(user);
        emailService.sendResetOtpEmail(email, otp);
    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) {
        UserEntity user = findByEmailOrThrow(email);
        otpService.verifyRegisterOtp(user, otp);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // ================= EMAIL CHANGE =================
    @Override
    public void requestEmailChange(String currentEmail, String newEmail) {

        UserEntity user = findByEmailOrThrow(currentEmail);

        String token = UUID.randomUUID().toString();

        user.setPendingEmail(newEmail);
        user.setEmailChangeToken(token);
        user.setEmailChangeExpiry(LocalDateTime.now().plusMinutes(10));

        userRepository.save(user);

        String link = BASE_URL + "/api/v1.0/profile/verify-email-change?token=" + token;

        emailService.sendVerificationEmail(newEmail, link);
    }

    @Override
    public void verifyEmailChange(String token) {

        UserEntity user = userRepository.findAll().stream()
                .filter(u -> token.equals(u.getEmailChangeToken()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (user.getEmailChangeExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        user.setEmail(user.getPendingEmail());
        user.setPendingEmail(null);
        user.setEmailChangeToken(null);

        userRepository.save(user);
    }

    @Override
    public void resendEmailChange(String email) {

        UserEntity user = findByEmailOrThrow(email);

        if (user.getPendingEmail() == null) {
            throw new RuntimeException("No pending email change");
        }

        requestEmailChange(email, user.getPendingEmail());
    }

    // ================= PHONE OTP =================
    @Override
    public void sendPhoneOtp(String email, String phoneNumber) {

        UserEntity user = findByEmailOrThrow(email);

        String otp = String.valueOf((int)(Math.random() * 900000) + 100000);

        user.setPhoneNumber(phoneNumber);
        user.setPhoneOtp(otp);
        user.setPhoneOtpExpiry(LocalDateTime.now().plusMinutes(5));

        userRepository.save(user);

        smsService.sendVerificationOtp(phoneNumber, otp);
    }

    @Override
    public void verifyPhoneOtp(String email, String otp) {

        UserEntity user = findByEmailOrThrow(email);

        if (!otp.equals(user.getPhoneOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        if (user.getPhoneOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        user.setPhoneVerified(true);
        user.setPhoneOtp(null);

        userRepository.save(user);
    }

    // ================= KYC =================
    @Override
    public String getKycRejectionReason(String email) {

        UserEntity user = findByEmailOrThrow(email);

        return kycRepository.findByUser(user)
                .map(KycEntity::getRejectionReason)
                .orElse(null);
    }

    // ================= LAST LOGIN =================
    @Override
    public Object getLastLogin(String email) {
        return findByEmailOrThrow(email).getUpdatedAt();
    }

    // ================= PROFILE =================
    @Override
    public ProfileResponse getProfile(String email) {
        return convertToProfileResponse(findByEmailOrThrow(email));
    }

    // ================= HELPERS =================
    private UserEntity findByEmailOrThrow(String email) {
        return userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private ProfileResponse convertToProfileResponse(UserEntity user) {

        Optional<KycEntity> kycOpt = kycRepository.findByUser(user);

        return ProfileResponse.builder()
                .userId(user.getUserId())
                .name(user.getEntityName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .isAccountVerified(Boolean.TRUE.equals(user.getEmailVerified()))
                .isKycVerified(kycOpt.map(k -> "VERIFIED".equalsIgnoreCase(k.getStatus())).orElse(false))
                .referralCode(user.getReferralCode())
                .build();
    }

    // ================= REQUIRED =================
    @Override
    public String getLoggedInUserId(String email) {
        return findByEmailOrThrow(email).getUserId();
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email.toLowerCase().trim());
    }

    @Override
    public UserEntity findByEmail(String email) {
        return findByEmailOrThrow(email);
    }

    @Override
    public UserEntity save(UserEntity userEntity) {
        return userRepository.save(userEntity);
    }
}