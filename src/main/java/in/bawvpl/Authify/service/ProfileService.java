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
}