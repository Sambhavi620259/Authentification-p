package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.UserEntity;

public interface OtpService {

    // LOGIN OTP
    String generateLoginOtp(UserEntity user);
    void verifyLoginOtp(UserEntity user, String otp);

    // REGISTER OTP
    String generateRegisterOtp(UserEntity user);
    void verifyRegisterOtp(UserEntity user, String otp); // ✅ ADD THIS

    // GENERIC OTP
    String generateOtp();
}