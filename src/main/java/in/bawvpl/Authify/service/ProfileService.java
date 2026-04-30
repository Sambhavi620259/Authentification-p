package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.io.ProfileRequest;
import in.bawvpl.Authify.io.ProfileResponse;

public interface ProfileService {

    // ================= PROFILE =================
    ProfileResponse createProfile(ProfileRequest request);
    ProfileResponse getProfile(String email);
    String getLoggedInUserId(String email);

    // ================= USER =================
    boolean existsByEmail(String email);
    UserEntity findByEmail(String email);
    UserEntity save(UserEntity userEntity);

    // ================= OTP =================
    void sendVerificationOtp(String email);
    void verifyEmailOtp(String email, String otp);

    void sendResetOtp(String email);
    void resetPassword(String email, String otp, String newPassword);

    // ================= EMAIL CHANGE =================
    void requestEmailChange(String currentEmail, String newEmail);
    void verifyEmailChangeOtp(String email, String otp);
    void resendEmailChangeOtp(String email);

    // ================= PHONE =================
    void sendPhoneOtp(String email, String phoneNumber);
    void verifyPhoneOtp(String email, String otp);

    // ================= KYC =================
    String getKycRejectionReason(String email);

    // ================= LAST LOGIN =================
    String getLastLogin(String email);
}