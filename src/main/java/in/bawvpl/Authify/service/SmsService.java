package in.bawvpl.Authify.service;

public interface SmsService {

    // 🔹 Generic sender (best practice)
    void sendSms(String phoneNumber, String message);

    // 🔹 OTP helpers
    void sendVerificationOtp(String phoneNumber, String otp);

    void sendResetOtp(String phoneNumber, String otp);
}