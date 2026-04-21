package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.ReferralEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ReferralRepository extends JpaRepository<ReferralEntity, Long> {

    @Query("SELECT COUNT(r) FROM ReferralEntity r WHERE r.referrer.id = :userId")
    Integer countByReferrerId(@Param("userId") Long userId);
}