package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.TransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    // 🔥 Used in TransactionService
    Page<TransactionEntity> findByUserId(Long userId, Pageable pageable);

    Page<TransactionEntity> findByUserIdAndStatus(Long userId, String status, Pageable pageable);

    // 🔥 Used in DashboardService / PaymentService
    Page<TransactionEntity> findByUser_IdOrderByPaymentDateDesc(Long userId, Pageable pageable);
}