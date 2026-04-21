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

    private static final String BASE_URL = "http://43.205.116.38:8080";

    // ================= REGISTER =================
    @Override
    @Transactional
    public ProfileResponse createProfile(ProfileRequest request) {

        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request is empty");
        }

        String email = request.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone already exists");
        }

        UserEntity user = UserEntity.builder()
                .userId("USR-" + UUID.randomUUID().toString().substring(0, 8))
                .entityType("INDIVIDUAL")
                .entityName(request.getName())
                .contactPerson(request.getName())
                .email(email)
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("ROLE_USER")
                .address(request.getAddress())
                .referralCode(request.getReferralCode())
                .emailVerified(false)
                .build();

        user = userRepository.save(user);

        // ✅ SEND REGISTER OTP
        String otp = otpService.generateRegisterOtp(user);

        try {
            emailService.sendVerificationOtpEmail(user.getEmail(), otp);
        } catch (Exception e) {
            log.error("OTP email failed", e);
        }

        // ================= SAVE KYC =================
        if (request.getDocumentType() != null && request.getDocumentNumber() != null) {

            KycEntity kyc = KycEntity.builder()
                    .user(user)
                    .documentType(request.getDocumentType())
                    .documentNumber(request.getDocumentNumber())
                    .filePath("N/A")
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

        email = email.toLowerCase().trim();

        UserEntity user = findByEmailOrThrow(email);

        // ✅ FIXED

        otpService.verifyRegisterOtp(user, otp);

        user.setEmailVerified(true);
        userRepository.save(user);

        return "Email verified successfully";
    }

    // ================= SEND REGISTER OTP =================
    @Override
    public void sendVerificationOtp(String email) {

        email = email.toLowerCase().trim();

        UserEntity user = findByEmailOrThrow(email);

        String otp = otpService.generateRegisterOtp(user);

        try {
            emailService.sendVerificationOtpEmail(user.getEmail(), otp);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send OTP");
        }
    }

    // ================= SEND RESET OTP =================
    @Override
    public void sendResetOtp(String email) {

        email = email.toLowerCase().trim();

        UserEntity user = findByEmailOrThrow(email);

        String otp = otpService.generateRegisterOtp(user);

        try {
            emailService.sendResetOtpEmail(user.getEmail(), otp);
        } catch (Exception e) {
            log.error("Reset OTP failed", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send reset OTP");
        }
    }

    // ================= RESET PASSWORD =================
    @Override
    @Transactional
    public void resetPassword(String email, String otp, String newPassword) {

        email = email.toLowerCase().trim();

        UserEntity user = findByEmailOrThrow(email);

        // ✅ FIXED (SECURITY)
        otpService.verifyRegisterOtp(user, otp);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // ================= GET PROFILE =================
    @Override
    public ProfileResponse getProfile(String email) {
        return convertToProfileResponse(findByEmailOrThrow(email.toLowerCase().trim()));
    }

    // ================= HELPERS =================
    private UserEntity findByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    // ================= PROFILE MAPPER =================
    private ProfileResponse convertToProfileResponse(UserEntity user) {

        Optional<KycEntity> kycOpt = kycRepository.findByUser(user);

        String documentType = null;
        String documentNumber = null;
        String kycStatus = "PENDING";
        String filePath = null;
        boolean isKycVerified = false;

        if (kycOpt.isPresent()) {
            KycEntity kyc = kycOpt.get();

            documentType = kyc.getDocumentType();
            documentNumber = kyc.getDocumentNumber();
            kycStatus = kyc.getStatus();
            filePath = kyc.getFilePath();

            isKycVerified = "VERIFIED".equalsIgnoreCase(kycStatus);
        }

        // ================= IMAGE =================
        String photoUrl = null;

        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isBlank()) {

            if (user.getPhotoUrl().startsWith("http")) {
                photoUrl = user.getPhotoUrl();
            } else if (user.getPhotoUrl().startsWith("/uploads")) {
                photoUrl = BASE_URL + user.getPhotoUrl();
            } else {
                photoUrl = BASE_URL + "/uploads/" + user.getPhotoUrl();
            }
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
                .photoUrl(photoUrl)
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
        return findByEmailOrThrow(email.toLowerCase().trim());
    }

    @Override
    public UserEntity save(UserEntity userEntity) {
        return userRepository.save(userEntity);
    }
}