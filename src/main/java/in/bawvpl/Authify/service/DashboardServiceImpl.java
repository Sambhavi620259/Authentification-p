package in.bawvpl.Authify.service;

import in.bawvpl.Authify.io.*;
import in.bawvpl.Authify.entity.*;
import in.bawvpl.Authify.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final TransactionRepository transactionRepository;
    private final ApplicationRepository applicationRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final WalletRepository walletRepository;
    private final ReferralRepository referralRepository;
    private final KycRepository kycRepository;
    private final NotificationRepository notificationRepository;
    private final ActivityLogRepository activityLogRepository;

    @Override
    public DashboardSummaryResponse getSummary(Long userId) {

        List<TransactionEntity> transactions =
                transactionRepository.findByUser_Id(userId, PageRequest.of(0, 100)).getContent();
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

    @Override
    public Page<TransactionResponse> getTransactions(Long userId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return transactionRepository
                .findByUser_IdOrderByPaymentDateDesc(userId, pageable)
                .map(tx -> TransactionResponse.builder()
                        .amount(tx.getAmount())
                        .status(tx.getStatus())
                        .type(tx.getType())
                        .date(tx.getPaymentDate())
                        .build());
    }

    @Override
    public Page<NotificationResponse> getNotifications(Long userId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return notificationRepository
                .findByUser_IdOrderByCreatedAtDesc(userId, pageable)
                .map(n -> NotificationResponse.builder()
                        .title(n.getTitle())
                        .message(n.getMessage())
                        .read(n.isRead())
                        .createdAt(n.getCreatedAt())
                        .build());
    }

    @Override
    public Page<ActivityResponse> getActivities(Long userId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return activityLogRepository
                .findByUser_IdOrderByTimestampDesc(userId, pageable)
                .map(a -> ActivityResponse.builder()
                        .action(a.getAction())
                        .description(a.getDescription())
                        .timestamp(a.getTimestamp())
                        .build());
    }
}