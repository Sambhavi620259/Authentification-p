package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.TransactionEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    // ================= BASIC =================
    Page<TransactionEntity> findByUser_Id(Long userId, Pageable pageable);

    // ================= DASHBOARD (IMPORTANT) =================
    Page<TransactionEntity> findByUser_IdOrderByPaymentDateDesc(Long userId, Pageable pageable);
}