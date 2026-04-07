package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.AppEntity;
import in.bawvpl.Authify.entity.TransactionEntity;
import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.repository.AppRepository;
import in.bawvpl.Authify.repository.TransactionRepository;
import in.bawvpl.Authify.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final AppRepository appRepository;

    // ================= CREATE PAYMENT =================
    public TransactionEntity createPayment(
            Long userId,
            Long appId,
            String method,
            String source,
            Double amount
    ) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        AppEntity app = appRepository.findById(appId)
                .orElseThrow(() -> new RuntimeException("App not found"));

        TransactionEntity transaction = TransactionEntity.builder()
                .user(user)
                .app(app)
                // ✅ SAFE (no getName error)
                .paymentDescription("Payment for App ID: " + app.getAppId())
                .paymentMethod(method)
                .paymentSource(source)
                .amount(amount)
                .paymentStatus("Completed")
                .build();

        return transactionRepository.save(transaction);
    }

    // ================= GET USER PAYMENTS =================
    public List<TransactionEntity> getUserPayments(Long userId) {

        // ✅ FIXED (entityId → id)
        return transactionRepository
                .findByUser_IdOrderByPaymentDateDesc(userId);
    }

    // ================= UPDATE STATUS =================
    public TransactionEntity updateStatus(Long id, String status) {

        TransactionEntity tx = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        tx.setPaymentStatus(status);

        return transactionRepository.save(tx);
    }
}