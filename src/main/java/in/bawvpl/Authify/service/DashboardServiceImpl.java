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
    private final ReferralRepository referralRepository;
    private final KycRepository kycRepository;
    private final NotificationRepository notificationRepository;
    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    // ================= SUMMARY =================

    @Override
    public DashboardSummaryResponse getSummaryByEmail(String email) {

        log.info("📊 Fetching dashboard summary for {}", email);

        UserEntity user = getUser(email);

        return getSummary(user.getId());
    }

    private DashboardSummaryResponse getSummary(Long userId) {

        try {

            Pageable pageable = PageRequest.of(0, 50);

            Page<TransactionEntity> txPage =
                    transactionRepository.findByUser_IdOrderByPaymentDateDesc(userId, pageable);

            List<TransactionEntity> transactions =
                    txPage != null ? txPage.getContent() : List.of();

            double totalSpent = transactions.stream()
                    .filter(t -> "DEBIT".equalsIgnoreCase(t.getType()))
                    .mapToDouble(t -> t.getAmount() != null ? t.getAmount() : 0.0)
                    .sum();

            double totalReceived = transactions.stream()
                    .filter(t -> "CREDIT".equalsIgnoreCase(t.getType()))
                    .mapToDouble(t -> t.getAmount() != null ? t.getAmount() : 0.0)
                    .sum();

            long totalApps = applicationRepository.countByUser_Id(userId);

            long activeSubs = subscriptionRepository
                    .countByUser_IdAndStatusIgnoreCase(userId, "ACTIVE");

            long referrals = referralRepository.countByReferrer_Id(userId);

            Double walletBalance = 0.0;

            String kycStatus = kycRepository.findByUser_Id(userId)
                    .map(KycEntity::getStatus)
                    .filter(s -> s != null && !s.isBlank())
                    .orElse("PENDING");

            return DashboardSummaryResponse.builder()
                    .totalApps((int) totalApps)
                    .activeSubscriptions((int) activeSubs)
                    .walletBalance(walletBalance)
                    .totalTransactions(transactions.size())
                    .referralCount((int) referrals)
                    .kycStatus(kycStatus)
                    .totalSpent(totalSpent)
                    .totalReceived(totalReceived)
                    .build();

        } catch (Exception e) {

            log.error("❌ Dashboard error: ", e);

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to fetch dashboard"
            );
        }
    }

    // ================= TRANSACTIONS =================

    @Override
    public Page<TransactionResponse> getTransactionsByEmail(String email, int page, int size) {

        UserEntity user = getUser(email);

        Pageable pageable = PageRequest.of(
                page,
                Math.min(size, 50),
                Sort.by("paymentDate").descending()
        );

        return transactionRepository
                .findByUser_IdOrderByPaymentDateDesc(user.getId(), pageable)
                .map(tx -> TransactionResponse.builder()
                        .id(tx.getId())
                        .amount(tx.getAmount())
                        .type(tx.getType())
                        .status(tx.getStatus())
                        .paymentDate(tx.getPaymentDate())
                        .paymentMethod(tx.getPaymentMethod())
                        .paymentSource(tx.getPaymentSource())
                        .paymentDescription(tx.getPaymentDescription())
                        .build()
                );
    }

    // ================= NOTIFICATIONS =================

    @Override
    public Page<NotificationResponse> getNotificationsByEmail(String email, int page, int size) {

        UserEntity user = getUser(email);

        Pageable pageable = PageRequest.of(
                page,
                Math.min(size, 50),
                Sort.by("createdAt").descending()
        );

        return notificationRepository
                .findByUser_IdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(n -> NotificationResponse.builder()
                        .title(n.getTitle())
                        .message(n.getMessage())
                        .read(n.getRead())
                        .createdAt(n.getCreatedAt())
                        .build());
    }

    // ================= ACTIVITY (🔥 FIXED) =================

    @Override
    public Page<ActivityResponse> getActivity(String email, int page, int size) {

        UserEntity user = getUser(email);

        Pageable pageable = PageRequest.of(
                page,
                Math.min(size, 50),
                Sort.by("timestamp").descending()
        );

        return activityLogRepository
                .findByUser_IdOrderByTimestampDesc(user.getId(), pageable)
                .map(a -> ActivityResponse.builder()
                        .action(a.getAction())
                        .description(a.getDescription())
                        .timestamp(a.getTimestamp())
                        .build());
    }

    // ================= HELPER =================

    private UserEntity getUser(String email) {

        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}