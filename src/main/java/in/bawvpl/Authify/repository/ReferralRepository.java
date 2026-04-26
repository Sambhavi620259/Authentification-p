package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.ReferralEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReferralRepository extends JpaRepository<ReferralEntity, Long> {

    // ✅ Safe + consistent (never null)
    long countByReferrer_Id(Long userId);
}