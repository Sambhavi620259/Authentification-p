package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    // ✅ FIXED
    List<TransactionEntity> findByUser_Id(Long userId);

    // ✅ FIXED
    List<TransactionEntity> findByUser_IdOrderByPaymentDateDesc(Long userId);
}