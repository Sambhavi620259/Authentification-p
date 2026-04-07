package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
    List<UserSubscription> findByUserId(String userId);
    Optional<UserSubscription> findByUserIdAndPlanId(String userId, String planId);
}

