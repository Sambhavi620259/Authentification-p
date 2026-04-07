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

    private static final int LOGIN_OTP_EXPIRY_MINUTES = 5;
    private static final int REGISTER_OTP_EXPIRY_MINUTES = 5;

    // ✅ GENERATE RANDOM OTP
    @Override
    public String generateOtp() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));
    }

    // ===============================
    // LOGIN OTP
    // ===============================
    @Override
    @Transactional
    public String generateLoginOtp(UserEntity user) {

        String otp = generateOtp();

        OtpVerification otpEntity = new OtpVerification();
        otpEntity.setUserId(user.getId()); // ✅ FIXED
        otpEntity.setEmail(user.getEmail());
        otpEntity.setPhoneNumber(user.getMobile());
        otpEntity.setOtp(otp);
        otpEntity.setPurpose("LOGIN");
        otpEntity.setExpiryTime(LocalDateTime.now().plusMinutes(LOGIN_OTP_EXPIRY_MINUTES));
        otpEntity.setIsUsed(false);

        otpRepository.saveAndFlush(otpEntity);

        log.info("🔥 LOGIN OTP SAVED for {} => {}", user.getEmail(), otp);

        return otp;
    }

    // ===============================
    // REGISTER OTP
    // ===============================
    @Override
    @Transactional
    public String generateRegisterOtp(UserEntity user) {

        String otp = generateOtp();

        OtpVerification otpEntity = new OtpVerification();
        otpEntity.setUserId(user.getId()); // ✅ FIXED
        otpEntity.setEmail(user.getEmail());
        otpEntity.setPhoneNumber(user.getMobile());
        otpEntity.setOtp(otp);
        otpEntity.setPurpose("REGISTER");
        otpEntity.setExpiryTime(LocalDateTime.now().plusMinutes(REGISTER_OTP_EXPIRY_MINUTES));
        otpEntity.setIsUsed(false);

        otpRepository.saveAndFlush(otpEntity);

        log.info("🔥 REGISTER OTP SAVED for {} => {}", user.getEmail(), otp);

        return otp;
    }

    // ===============================
    // VERIFY LOGIN OTP
    // ===============================
    @Override
    @Transactional
    public void verifyLoginOtp(UserEntity user, String otp) {

        OtpVerification otpEntity = otpRepository
                .findTopByEmailAndPurposeOrderByCreatedAtDesc(user.getEmail(), "LOGIN")
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP not found"));

        if (!otpEntity.getOtp().equals(otp)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP");
        }

        if (otpEntity.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP expired");
        }

        if (Boolean.TRUE.equals(otpEntity.getIsUsed())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP already used");
        }

        otpEntity.setIsUsed(true);
        otpRepository.save(otpEntity);

        log.info("✅ OTP verified for {}", user.getEmail());
    }
}