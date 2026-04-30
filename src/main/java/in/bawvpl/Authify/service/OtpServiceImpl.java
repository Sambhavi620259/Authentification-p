package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.OtpVerification;
import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.repository.OtpRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final OtpRepository otpRepository;

    private static final int EXPIRY_MINUTES = 5;

    // ================= GENERATE =================
    @Override
    public String generateOtp() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));
    }

    // ================= LOGIN =================
    @Override
    @Transactional
    public String generateLoginOtp(UserEntity user) {
        return generate(user, "LOGIN");
    }

    @Override
    public void verifyLoginOtp(UserEntity user, String otp) {
        validate(user, otp, "LOGIN");
    }

    // ================= REGISTER =================
    @Override
    @Transactional
    public String generateRegisterOtp(UserEntity user) {
        return generate(user, "REGISTER");
    }

    @Override
    public void verifyRegisterOtp(UserEntity user, String otp) {
        validate(user, otp, "REGISTER");
    }

    // ================= RESET =================
    @Override
    @Transactional
    public String generateResetOtp(UserEntity user) {
        return generate(user, "RESET");
    }

    @Override
    public void verifyResetOtp(UserEntity user, String otp) {
        validate(user, otp, "RESET");
    }

    // ================= PHONE =================
    @Override
    @Transactional
    public String generatePhoneOtp(UserEntity user) {
        return generate(user, "PHONE");
    }

    @Override
    public void verifyPhoneOtp(UserEntity user, String otp) {
        validate(user, otp, "PHONE");
    }

    // ================= COMMON GENERATE =================
    private String generate(UserEntity user, String purpose) {

        String email = normalize(user.getEmail());
        String otp = generateOtp();

        invalidateOldOtp(email, purpose);

        OtpVerification entity = new OtpVerification();
        entity.setUserId(user.getId());
        entity.setEmail(email);
        entity.setPhoneNumber(user.getPhoneNumber());
        entity.setOtp(otp);
        entity.setPurpose(purpose);
        entity.setExpiryTime(LocalDateTime.now().plusMinutes(EXPIRY_MINUTES));
        entity.setIsUsed(false);

        otpRepository.save(entity);

        log.info("{} OTP generated for {}", purpose, email);

        return otp;
    }

    // ================= COMMON VERIFY =================
    private void validate(UserEntity user, String otp, String purpose) {

        if (otp == null || otp.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP required");
        }

        String email = normalize(user.getEmail());

        OtpVerification entity = otpRepository
                .findTopByEmailAndPurposeOrderByCreatedAtDesc(email, purpose)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP not found"));

        if (Boolean.TRUE.equals(entity.getIsUsed())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP already used");
        }

        if (!entity.getOtp().equals(otp)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP");
        }

        if (entity.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP expired");
        }

        entity.setIsUsed(true);
        otpRepository.save(entity);

        log.info("{} OTP verified for {}", purpose, email);
    }

    // ================= HELPERS =================
    private void invalidateOldOtp(String email, String purpose) {
        otpRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(email, purpose)
                .ifPresent(old -> {
                    old.setIsUsed(true);
                    otpRepository.save(old);
                });
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}