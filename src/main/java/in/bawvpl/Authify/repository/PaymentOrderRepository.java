package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.PaymentOrder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {

    // ================= USER ORDERS =================
    List<PaymentOrder> findByUser_IdOrderByCreatedAtDesc(Long userId);

    // ================= FIND BY ORDER ID =================
    Optional<PaymentOrder> findByOrderId(String orderId);

    // ================= FILTER BY STATUS =================
    List<PaymentOrder> findByUser_IdAndPaymentStatus(Long userId, String status);

    // ================= OPTIONAL =================
    List<PaymentOrder> findByPaymentStatus(String status);
}