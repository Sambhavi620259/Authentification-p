package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // ✅ Find by email (LOGIN)
    Optional<UserEntity> findByEmail(String email);

    // ✅ Check email exists
    boolean existsByEmail(String email);

    // ✅ Phone validation
    boolean existsByPhoneNumber(String phoneNumber);

    Optional<UserEntity> findByPhoneNumber(String phoneNumber);

    // ✅ REQUIRED for KYC Controller (VERY IMPORTANT)
    Optional<UserEntity> findByUserId(String userId);
}