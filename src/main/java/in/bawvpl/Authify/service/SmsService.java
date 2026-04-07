package in.bawvpl.Authify.service;

public interface SmsService {
    void sendVerificationOtp(String phoneNumber, String otp);
    void sendResetOtp(String phoneNumber, String otp);
}
