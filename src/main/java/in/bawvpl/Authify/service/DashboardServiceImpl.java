package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.TransactionEntity;
import in.bawvpl.Authify.io.DashboardSummaryResponse;
import in.bawvpl.Authify.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final TransactionRepository transactionRepository;

    @Override
    public DashboardSummaryResponse getSummary(String userId) {

        // ✅ convert String → Long
        Long userIdLong = Long.parseLong(userId);

        // ✅ FIXED
        List<TransactionEntity> all =
                transactionRepository.findByUser_Id(userIdLong);

        double totalSpent = 0;

        for (TransactionEntity tx : all) {
            totalSpent += tx.getAmount();
        }

        return DashboardSummaryResponse.builder()
                .totalBalance(0)
                .totalTransactions(all.size())
                .totalSpent(totalSpent)
                .totalReceived(0)
                .build();
    }

    @Override
    public List<TransactionEntity> getRecentTransactions(String userId, int limit) {

        Long userIdLong = Long.parseLong(userId);

        // ✅ FIXED
        return transactionRepository
                .findByUser_IdOrderByPaymentDateDesc(userIdLong)
                .stream()
                .limit(limit)
                .toList();
    }
}