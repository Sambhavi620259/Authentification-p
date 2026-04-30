package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.UserEntity;

public interface OtpService {

    // LOGIN
    String generateLoginOtp(UserEntity user);
    void verifyLoginOtp(UserEntity user, String otp);

    // REGISTER
    String generateRegisterOtp(UserEntity user);
    void verifyRegisterOtp(UserEntity user, String otp);

    // RESET PASSWORD
    String generateResetOtp(UserEntity user);
    void verifyResetOtp(UserEntity user, String otp);

    // PHONE
    String generatePhoneOtp(UserEntity user);
    void verifyPhoneOtp(UserEntity user, String otp);

    // GENERIC
    String generateOtp();
}