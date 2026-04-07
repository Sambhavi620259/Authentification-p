package in.bawvpl.Authify.service;

import in.bawvpl.Authify.io.DashboardSummaryResponse;
import in.bawvpl.Authify.entity.TransactionEntity;

import java.util.List;

public interface DashboardService {
    DashboardSummaryResponse getSummary(String userId);
    List<TransactionEntity> getRecentTransactions(String userId, int limit);
}
