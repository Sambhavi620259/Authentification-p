package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {

    @Query("SELECT COUNT(s) FROM SubscriptionEntity s WHERE s.user.id = :userId AND s.status = :status")
    Integer countByUserIdAndStatus(Long userId, String status);
}