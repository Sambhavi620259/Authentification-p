package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {

    // ✅ FIXED (RELATION BASED QUERY)
    List<PaymentOrder> findByUser_Id(Long userId);
}