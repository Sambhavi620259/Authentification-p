package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.TransactionEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    // ✅ Sorted transactions (USED in dashboard)
    Page<TransactionEntity> findByUser_IdOrderByPaymentDateDesc(Long userId, Pageable pageable);

    // ✅ Optional: total transaction count (useful for analytics)
    long countByUser_Id(Long userId);
}