package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // ================= AUTH =================

    Optional<UserEntity> findByEmail(String email); // legacy
    Optional<UserEntity> findByEmailIgnoreCase(String email); // ✅ USE THIS

    boolean existsByEmail(String email);
    boolean existsByEmailIgnoreCase(String email);

    // ================= PHONE =================

    boolean existsByPhoneNumber(String phoneNumber);
    Optional<UserEntity> findByPhoneNumber(String phoneNumber);

    // ================= OPTIONAL =================
    // ⚠️ USE ONLY IF YOU REALLY HAVE STRING userId FIELD
    Optional<UserEntity> findByUserId(String userId);

    // ================= EMAIL VERIFICATION =================

    Optional<UserEntity> findByVerificationToken(String verificationToken);

    // ================= REFERRAL =================

    Optional<UserEntity> findByReferralCode(String referralCode);
}