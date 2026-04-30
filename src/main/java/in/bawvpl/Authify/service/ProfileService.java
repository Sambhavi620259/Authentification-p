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

    String verifyOtp(String email, String otp);

    void sendVerificationOtp(String email);

    void sendResetOtp(String email);

    void resetPassword(String email, String otp, String newPassword);

    // ================= EMAIL CHANGE =================

    void requestEmailChange(String currentEmail, String newEmail);

    void verifyEmailChange(String token);

    void resendEmailChange(String email);

    // ================= PHONE CHANGE =================

    void sendPhoneOtp(String email, String phoneNumber);

    void verifyPhoneOtp(String email, String otp);

    // ================= KYC =================

    String getKycRejectionReason(String email);

    // ================= LAST LOGIN =================

    Object getLastLogin(String email);
}