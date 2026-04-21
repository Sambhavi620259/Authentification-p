package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.ApplicationEntity;
import in.bawvpl.Authify.entity.TransactionEntity;
import in.bawvpl.Authify.entity.UserEntity;

import in.bawvpl.Authify.repository.ApplicationRepository;
import in.bawvpl.Authify.repository.TransactionRepository;
import in.bawvpl.Authify.repository.UserRepository;
import in.bawvpl.Authify.repository.UserApplicationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final UserApplicationRepository userAppRepository;

    // ================= CREATE PAYMENT =================
    @Transactional
    public TransactionEntity createPayment(
            String email,
            Long appId,
            String method,
            String source,
            Double amount
    ) {

        email = email.toLowerCase().trim();

        if (amount == null || amount <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid amount");
        }

        if (method == null || method.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment method required");
        }

        // ✅ USER
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        // ✅ APP
        ApplicationEntity app = applicationRepository.findById(appId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "App not found"));

        // ✅ CREATE TRANSACTION
        TransactionEntity transaction = TransactionEntity.builder()
                .user(user)
                .app(app)
                .paymentDescription("Payment for App: " + app.getAppName())
                .paymentMethod(method)
                .paymentSource(source)
                .amount(amount)
                .status("PENDING") // ✅ FIXED
                .type("DEBIT")
                .build();

        return transactionRepository.save(transaction);
    }

    // ================= GET USER PAYMENTS =================
    public List<TransactionEntity> getUserPayments(String email) {

        email = email.toLowerCase().trim();

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        return transactionRepository
                .findByUser_IdOrderByPaymentDateDesc(
                        user.getId(),
                        PageRequest.of(0, 10)
                )
                .getContent();
    }

    // ================= UPDATE STATUS =================
    @Transactional
    public TransactionEntity updateStatus(Long id, String status) {

        TransactionEntity tx = transactionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Transaction not found"));

        tx.setStatus(status); // ✅ FIXED

        // ✅ AUTO ACTIVATE APP
        if ("SUCCESS".equalsIgnoreCase(status)) {

            UserEntity user = tx.getUser();
            ApplicationEntity app = tx.getApp();

            userAppRepository.findByUser_IdAndApp_AppId(user.getId(), app.getAppId())
                    .ifPresent(userApp -> {
                        userApp.setSubscriptionStatus("ACTIVE");
                        userAppRepository.save(userApp);
                    });

            log.info("App activated for user [{}] app [{}]",
                    user.getEmail(), app.getAppId());
        }

        return transactionRepository.save(tx);
    }
}