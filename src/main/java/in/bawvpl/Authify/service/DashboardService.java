package in.bawvpl.Authify.service;

import in.bawvpl.Authify.io.DashboardSummaryResponse;
import in.bawvpl.Authify.io.NotificationResponse;
import in.bawvpl.Authify.io.TransactionResponse;
import in.bawvpl.Authify.io.ActivityResponse;
import org.springframework.data.domain.Page;

public interface DashboardService {

    DashboardSummaryResponse getSummary(Long userId);

    Page<TransactionResponse> getTransactions(Long userId, int page, int size);

    Page<NotificationResponse> getNotifications(Long userId, int page, int size);

    Page<ActivityResponse> getActivities(Long userId, int page, int size);
}
