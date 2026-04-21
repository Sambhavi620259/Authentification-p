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

    private static final int LOGIN_EXPIRY = 5;
    private static final int REGISTER_EXPIRY = 5;

    // ================= GENERATE OTP =================
    @Override
    public String generateOtp() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));
    }

    // ================= LOGIN OTP =================
    @Override
    @Transactional
    public String generateLoginOtp(UserEntity user) {

        String email = user.getEmail().toLowerCase().trim();
        String otp = generateOtp();

        otpRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(email, "LOGIN")
                .ifPresent(oldOtp -> {
                    oldOtp.setIsUsed(true);
                    otpRepository.save(oldOtp);
                });

        OtpVerification entity = new OtpVerification();
        entity.setUserId(user.getId());
        entity.setEmail(email);
        entity.setPhoneNumber(user.getPhoneNumber());
        entity.setOtp(otp);
        entity.setPurpose("LOGIN");
        entity.setExpiryTime(LocalDateTime.now().plusMinutes(LOGIN_EXPIRY));
        entity.setIsUsed(false);

        otpRepository.save(entity);

        log.info("LOGIN OTP generated for {}", email);

        return otp;
    }

    // ================= VERIFY LOGIN OTP =================
    @Override
    @Transactional
    public void verifyLoginOtp(UserEntity user, String otp) {

        validateOtp(user, otp, "LOGIN");
    }

    // ================= REGISTER OTP =================
    @Override
    @Transactional
    public String generateRegisterOtp(UserEntity user) {

        String email = user.getEmail().toLowerCase().trim();
        String otp = generateOtp();

        otpRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(email, "REGISTER")
                .ifPresent(oldOtp -> {
                    oldOtp.setIsUsed(true);
                    otpRepository.save(oldOtp);
                });

        OtpVerification entity = new OtpVerification();
        entity.setUserId(user.getId());
        entity.setEmail(email);
        entity.setPhoneNumber(user.getPhoneNumber());
        entity.setOtp(otp);
        entity.setPurpose("REGISTER");
        entity.setExpiryTime(LocalDateTime.now().plusMinutes(REGISTER_EXPIRY));
        entity.setIsUsed(false);

        otpRepository.save(entity);

        log.info("REGISTER OTP generated for {}", email);

        return otp;
    }

    // ================= VERIFY REGISTER OTP =================
    @Override
    @Transactional
    public void verifyRegisterOtp(UserEntity user, String otp) {

        validateOtp(user, otp, "REGISTER");
    }

    // ================= COMMON VALIDATION =================
    private void validateOtp(UserEntity user, String otp, String purpose) {

        String email = user.getEmail().toLowerCase().trim();

        OtpVerification entity = otpRepository
                .findTopByEmailAndPurposeOrderByCreatedAtDesc(email, purpose)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP not found"));

        if (otp == null || otp.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP required");
        }

        if (!entity.getOtp().equals(otp)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP");
        }

        if (entity.getExpiryTime() == null ||
                entity.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP expired");
        }

        if (Boolean.TRUE.equals(entity.getIsUsed())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP already used");
        }

        entity.setIsUsed(true);
        otpRepository.save(entity);

        log.info("{} OTP verified for {}", purpose, email);
    }
}