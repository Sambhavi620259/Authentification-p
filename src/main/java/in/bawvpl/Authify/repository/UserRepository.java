package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // ================= AUTH =================

    // Find by email (LOGIN)
    Optional<UserEntity> findByEmail(String email);

    // Check email exists
    boolean existsByEmail(String email);

    // ================= PHONE =================

    // Phone validation
    boolean existsByPhoneNumber(String phoneNumber);

    Optional<UserEntity> findByPhoneNumber(String phoneNumber);

    // ================= USER =================

    // REQUIRED for KYC Controller
    Optional<UserEntity> findByUserId(String userId);

    // ================= EMAIL VERIFICATION =================

    // Find user by verification token
    Optional<UserEntity> findByVerificationToken(String verificationToken);

    // ================= REFERRAL =================

    // Find user by referral code
    Optional<UserEntity> findByReferralCode(String referralCode);
}