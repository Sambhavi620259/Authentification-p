package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // ================= AUTH =================

    // ✅ OLD METHODS (KEEP THESE — REQUIRED FOR YOUR CODE)
    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    // ✅ NEW METHODS (FOR FUTURE / SAFE USE)
    Optional<UserEntity> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    // ================= PHONE =================

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<UserEntity> findByPhoneNumber(String phoneNumber);

    // ================= USER =================

    Optional<UserEntity> findByUserId(String userId);

    // ================= EMAIL VERIFICATION =================

    Optional<UserEntity> findByVerificationToken(String verificationToken);

    // ================= REFERRAL =================

    Optional<UserEntity> findByReferralCode(String referralCode);
}