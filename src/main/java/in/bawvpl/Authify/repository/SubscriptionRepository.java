package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {

    // ✅ Safe + best practice (never null)
    long countByUser_IdAndStatusIgnoreCase(Long userId, String status);
}