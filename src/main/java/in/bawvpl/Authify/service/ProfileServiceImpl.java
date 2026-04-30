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

    // ================= REGISTER =================
    @Override
    @Transactional
    public ProfileResponse createProfile(ProfileRequest request) {

        String email = normalize(request.getEmail());

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

        return convert(user);
    }

    // ================= EMAIL VERIFY =================
    @Override
    public void verifyEmailOtp(String email, String otp) {

        UserEntity user = find(email);

        otpService.verifyRegisterOtp(user, otp);

        user.setEmailVerified(true);
        userRepository.save(user);
    }

    @Override
    public void sendVerificationOtp(String email) {

        UserEntity user = find(email);

        String otp = otpService.generateRegisterOtp(user);
        emailService.sendVerificationOtpEmail(user.getEmail(), otp);
    }

    // ================= RESET =================
    @Override
    public void sendResetOtp(String email) {

        UserEntity user = find(email);

        String otp = otpService.generateResetOtp(user);
        emailService.sendResetOtpEmail(user.getEmail(), otp);
    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) {

        UserEntity user = find(email);

        otpService.verifyResetOtp(user, otp);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // ================= EMAIL CHANGE (OTP BASED) =================
    @Override
    public void requestEmailChange(String currentEmail, String newEmail) {

        UserEntity user = find(currentEmail);

        newEmail = normalize(newEmail);

        if (userRepository.existsByEmail(newEmail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        String otp = otpService.generateOtp();

        user.setPendingEmail(newEmail);
        user.setEmailChangeOtp(otp);
        user.setEmailChangeExpiry(LocalDateTime.now().plusMinutes(5));

        userRepository.save(user);

        emailService.sendVerificationOtpEmail(newEmail, otp);
    }

    @Override
    public void verifyEmailChangeOtp(String email, String otp) {

        UserEntity user = find(email);

        if (user.getEmailChangeOtp() == null) {
            throw new RuntimeException("No email change requested");
        }

        if (!user.getEmailChangeOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        if (user.getEmailChangeExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        user.setEmail(user.getPendingEmail());
        user.setPendingEmail(null);
        user.setEmailChangeOtp(null);
        user.setEmailVerified(true);

        userRepository.save(user);
    }

    @Override
    public void resendEmailChangeOtp(String email) {

        UserEntity user = find(email);

        if (user.getPendingEmail() == null) {
            throw new RuntimeException("No pending email change");
        }

        requestEmailChange(email, user.getPendingEmail());
    }

    // ================= PHONE =================
    @Override
    public void sendPhoneOtp(String email, String phoneNumber) {

        UserEntity user = find(email);

        user.setPhoneNumber(phoneNumber);
        userRepository.save(user);

        String otp = otpService.generatePhoneOtp(user);

        smsService.sendVerificationOtp(phoneNumber, otp);
    }

    @Override
    public void verifyPhoneOtp(String email, String otp) {

        UserEntity user = find(email);

        otpService.verifyPhoneOtp(user, otp);

        user.setPhoneVerified(true);
        userRepository.save(user);
    }

    // ================= KYC =================
    @Override
    public String getKycRejectionReason(String email) {

        UserEntity user = find(email);

        return kycRepository.findByUser(user)
                .map(KycEntity::getRejectionReason)
                .orElse(null);
    }

    // ================= LAST LOGIN =================
    @Override
    public String getLastLogin(String email) {
        return find(email).getUpdatedAt().toString();
    }

    // ================= PROFILE =================
    @Override
    public ProfileResponse getProfile(String email) {
        return convert(find(email));
    }

    // ================= HELPERS =================
    private UserEntity find(String email) {
        return userRepository.findByEmail(normalize(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private ProfileResponse convert(UserEntity user) {

        Optional<KycEntity> kyc = kycRepository.findByUser(user);

        return ProfileResponse.builder()
                .userId(user.getUserId())
                .name(user.getEntityName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .isAccountVerified(Boolean.TRUE.equals(user.getEmailVerified()))
                .isKycVerified(kyc.map(k -> "VERIFIED".equalsIgnoreCase(k.getStatus())).orElse(false))
                .referralCode(user.getReferralCode())
                .build();
    }

    // ================= REQUIRED =================
    @Override
    public String getLoggedInUserId(String email) {
        return find(email).getUserId();
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(normalize(email));
    }

    @Override
    public UserEntity findByEmail(String email) {
        return find(email);
    }

    @Override
    public UserEntity save(UserEntity userEntity) {
        return userRepository.save(userEntity);
    }
}