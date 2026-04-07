package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.KycEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KycRepository extends JpaRepository<KycEntity, Long> {

    // ✅ FIX: use user object, NOT String
    List<KycEntity> findByUser_UserId(String userId);
}