package in.bawvpl.Authify.service;

import in.bawvpl.Authify.io.*;
import in.bawvpl.Authify.entity.*;
import in.bawvpl.Authify.repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final TransactionRepository transactionRepository;
    private final ApplicationRepository applicationRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final WalletRepository walletRepository;
    private final ReferralRepository referralRepository;
    private final KycRepository kycRepository;
    private final NotificationRepository notificationRepository;
    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    // ================= JWT BASED METHODS =================

    @Override
    public DashboardSummaryResponse getSummaryByEmail(String email) {

        log.info("Fetching dashboard summary for {}", email);

        UserEntity user = getUser(email);

        return getSummary(user.getId());
    }

    @Override
    public Page<TransactionResponse> getTransactionsByEmail(String email, int page, int size) {

        log.info("Fetching transactions for {} | page={} size={}", email, page, size);

        UserEntity user = getUser(email);

        return getTransactions(user.getId(), page, size);
    }

    @Override
    public Page<NotificationResponse> getNotificationsByEmail(String email, int page, int size) {

        UserEntity user = getUser(email);

        return getNotifications(user.getId(), page, size);
    }

    @Override
    public Page<ActivityResponse> getActivitiesByEmail(String email, int page, int size) {

        UserEntity user = getUser(email);

        return getActivities(user.getId(), page, size);
    }

    // ================= INTERNAL METHODS =================

    public DashboardSummaryResponse getSummary(Long userId) {

        Pageable pageable = PageRequest.of(0, 50, Sort.by("paymentDate").descending());

        List<TransactionEntity> transactions =
                transactionRepository.findByUser_IdOrderByPaymentDateDesc(userId, pageable)
                        .getContent();

        double totalSpent = transactions.stream()
                .filter(t -> "DEBIT".equalsIgnoreCase(t.getType()))
                .mapToDouble(TransactionEntity::getAmount)
                .sum();

        double totalReceived = transactions.stream()
                .filter(t -> "CREDIT".equalsIgnoreCase(t.getType()))
                .mapToDouble(TransactionEntity::getAmount)
                .sum();

        Integer totalApps = applicationRepository.countByUserId(userId);
        Integer activeSubs = subscriptionRepository.countByUserIdAndStatus(userId, "ACTIVE");
        Double walletBalance = walletRepository.findBalanceByUserId(userId);
        Integer referrals = referralRepository.countByReferrerId(userId);
        String kycStatus = kycRepository.findStatusByUser_Id(userId);

        return DashboardSummaryResponse.builder()
                .totalApps(totalApps != null ? totalApps : 0)
                .activeSubscriptions(activeSubs != null ? activeSubs : 0)
                .walletBalance(walletBalance != null ? walletBalance : 0.0)
                .totalTransactions(transactions.size())
                .referralCount(referrals != null ? referrals : 0)
                .kycStatus(kycStatus != null ? kycStatus : "PENDING")
                .totalSpent(totalSpent)
                .totalReceived(totalReceived)
                .build();
    }

    public Page<TransactionResponse> getTransactions(Long userId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("paymentDate").descending());

        return transactionRepository
                .findByUser_IdOrderByPaymentDateDesc(userId, pageable)
                .map(tx -> TransactionResponse.builder()
                        .amount(tx.getAmount())
                        .status(tx.getStatus())
                        .type(tx.getType())
                        .date(tx.getPaymentDate())
                        .build());
    }

    public Page<NotificationResponse> getNotifications(Long userId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return notificationRepository
                .findByUser_IdOrderByCreatedAtDesc(userId, pageable)
                .map(n -> NotificationResponse.builder()
                        .title(n.getTitle())
                        .message(n.getMessage())
                        .read(n.isRead())
                        .createdAt(n.getCreatedAt())
                        .build());
    }

    public Page<ActivityResponse> getActivities(Long userId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());

        return activityLogRepository
                .findByUser_IdOrderByTimestampDesc(userId, pageable)
                .map(a -> ActivityResponse.builder()
                        .action(a.getAction())
                        .description(a.getDescription())
                        .timestamp(a.getTimestamp())
                        .build());
    }

    // ================= HELPER =================
    private UserEntity getUser(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}