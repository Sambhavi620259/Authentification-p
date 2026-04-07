package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.KycEntity;
import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.io.ProfileRequest;
import in.bawvpl.Authify.io.ProfileResponse;
import in.bawvpl.Authify.repository.KycRepository;
import in.bawvpl.Authify.repository.UserRepository;
import in.bawvpl.Authify.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SmsService smsService;
    private final KycRepository kycRepository;
    private final JwtUtil jwtUtil;

    private static final long VERIFY_OTP_TTL_SECONDS = 5 * 60;
    private static final long RESET_OTP_TTL_SECONDS = 15 * 60;

    // ================= REGISTER =================
    @Override
    @Transactional
    public ProfileResponse createProfile(ProfileRequest request) {

        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request is empty");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        UserEntity user = UserEntity.builder()
                .userId(UUID.randomUUID().toString())
                .entityType("Individual")
                .entityName(request.getName()) // ✅ FIX
                .contactPerson(request.getName())
                .email(request.getEmail())
                .mobile(request.getPhoneNumber()) // ✅ FIX
                .password(passwordEncoder.encode(request.getPassword()))
                .adminRole("ROLE_USER")
                .emailVerified(false) // ✅ FIX
                .build();

        // 🔥 GENERATE OTP
        String otp = generateOtp();
        long expireAt = Instant.now().plusSeconds(VERIFY_OTP_TTL_SECONDS).toEpochMilli();

        user.setVerifyOtp(otp);
        user.setVerifyOtpExpireAt(expireAt);

        userRepository.save(user);

        // 🔥 SEND OTP
        emailService.sendVerificationOtpEmail(user.getEmail(), otp);

        // 🔥 SAVE KYC IF PROVIDED
        if (request.getAadhaarNumber() != null && request.getPanNumber() != null) {

            KycEntity kyc = KycEntity.builder()
                    .user(user)
                    .aadhaarNumber(request.getAadhaarNumber())
                    .panNumber(request.getPanNumber())
                    .status("PENDING")
                    .completed(false)
                    .uploadedAt(Instant.now())
                    .build();

            kycRepository.save(kyc);
        }

        return convertToProfileResponse(user);
    }

    // ================= VERIFY OTP =================
    @Override
    @Transactional
    public String verifyOtp(String email, String otp) {

        UserEntity user = findByEmailOrThrow(email);

        if (user.getVerifyOtp() == null || !otp.equals(user.getVerifyOtp())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP");
        }

        if (user.getVerifyOtpExpireAt() == null ||
                Instant.now().toEpochMilli() > user.getVerifyOtpExpireAt()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP expired");
        }

        user.setEmailVerified(true); // ✅ FIX
        user.setVerifyOtp(null);
        user.setVerifyOtpExpireAt(null);

        userRepository.save(user);

        return jwtUtil.generateAccessToken(user.getEmail());
    }

    // ================= SEND VERIFICATION OTP =================
    @Override
    public void sendVerificationOtp(String email) {

        UserEntity user = findByEmailOrThrow(email);

        String otp = generateOtp();
        long expireAt = Instant.now().plusSeconds(VERIFY_OTP_TTL_SECONDS).toEpochMilli();

        user.setVerifyOtp(otp);
        user.setVerifyOtpExpireAt(expireAt);

        userRepository.save(user);

        emailService.sendVerificationOtpEmail(email, otp);
    }

    // ================= RESET OTP =================
    @Override
    public void sendResetOtp(String email) {

        UserEntity user = findByEmailOrThrow(email);

        String otp = generateOtp();
        long expireAt = Instant.now().plusSeconds(RESET_OTP_TTL_SECONDS).toEpochMilli();

        user.setResetOtp(otp);
        user.setResetOtpExpireAt(expireAt);

        userRepository.save(user);

        emailService.sendResetOtpEmail(email, otp);
    }

    // ================= RESET PASSWORD =================
    @Override
    @Transactional
    public void resetPassword(String email, String otp, String newPassword) {

        UserEntity user = findByEmailOrThrow(email);

        if (user.getResetOtp() == null || !otp.equals(user.getResetOtp())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP");
        }

        if (user.getResetOtpExpireAt() == null ||
                Instant.now().toEpochMilli() > user.getResetOtpExpireAt()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetOtp(null);
        user.setResetOtpExpireAt(null);

        userRepository.save(user);
    }

    // ================= GET PROFILE =================
    @Override
    public ProfileResponse getProfile(String email) {
        UserEntity user = findByEmailOrThrow(email);
        return convertToProfileResponse(user);
    }

    // ================= VERIFY KYC =================
    @Override
    @Transactional
    public void verifyKyc(String email) {

        UserEntity user = findByEmailOrThrow(email);

        // ✅ FIX (no direct boolean field now)
        // KYC is considered verified if entity exists
        if (user.getKyc() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "KYC not found");
        }

        userRepository.save(user);
    }

    // ================= REQUIRED METHOD (FIXED ERROR) =================
    @Override
    public UserEntity save(UserEntity userEntity) {
        return userRepository.save(userEntity);
    }

    // ================= HELPERS =================

    private UserEntity findByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private String generateOtp() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));
    }

    private ProfileResponse convertToProfileResponse(UserEntity user) {

        return ProfileResponse.builder()
                .userId(user.getUserId())
                .name(user.getEntityName()) // ✅ FIX
                .email(user.getEmail())
                .phoneNumber(user.getMobile()) // ✅ FIX
                .isAccountVerified(Boolean.TRUE.equals(user.getEmailVerified())) // ✅ FIX
                .isKycVerified(user.getKyc() != null) // ✅ FIX
                .build();
    }

    // ================= EXTRA =================

    @Override
    public String getLoggedInUserId(String email) {
        return findByEmailOrThrow(email).getUserId();
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public UserEntity findByEmail(String email) {
        return findByEmailOrThrow(email);
    }
}