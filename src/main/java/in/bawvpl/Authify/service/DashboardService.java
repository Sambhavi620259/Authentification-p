package in.bawvpl.Authify.service;

import in.bawvpl.Authify.io.*;

import org.springframework.data.domain.Page;

public interface DashboardService {

    // ================= SUMMARY =================
    DashboardSummaryResponse getSummaryByEmail(String email);

    // ================= TRANSACTIONS =================
    Page<TransactionResponse> getTransactionsByEmail(String email, int page, int size);

    // ================= NOTIFICATIONS =================
    Page<NotificationResponse> getNotificationsByEmail(String email, int page, int size);

    // ================= ACTIVITIES =================
    Page<ActivityResponse> getActivitiesByEmail(String email, int page, int size);
}