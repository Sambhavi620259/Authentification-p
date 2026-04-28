package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.*;
import in.bawvpl.Authify.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    // ================= CREATE ORDER =================
    public PaymentOrder createOrder(String email, Long appId, Double amount) {

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PaymentOrder order = PaymentOrder.builder()
                .user(user)
                .orderId("ORD_" + UUID.randomUUID())
                .paymentMethod("UPI")
                .paymentStatus("CREATED")
                .amount(amount)
                .build();

        return paymentOrderRepository.save(order);
    }

    // ================= VERIFY PAYMENT =================
    public String verifyPayment(String orderId, String status) {

        PaymentOrder order = paymentOrderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (status.equalsIgnoreCase("SUCCESS")) {

            order.setPaymentStatus("SUCCESS");
            paymentOrderRepository.save(order);

            // ✅ CREATE TRANSACTION ONLY AFTER SUCCESS
            TransactionEntity txn = TransactionEntity.builder()
                    .user(order.getUser())
                    .app(order.getApp())
                    .amount(order.getAmount())
                    .paymentMethod("UPI")
                    .paymentSource("GPay/PhonePe")
                    .status("SUCCESS")
                    .type("DEBIT")
                    .paymentDescription("App purchase")
                    .build();

            transactionRepository.save(txn);

            return "Payment Success";

        } else {
            order.setPaymentStatus("FAILED");
            paymentOrderRepository.save(order);

            return "Payment Failed";
        }
    }
}