package in.bawvpl.Authify.service;

import in.bawvpl.Authify.io.*;

import org.springframework.data.domain.Page;

/**
 * Dashboard Service (JWT-based)
 *
 * All methods operate using authenticated user's email
 * extracted from JWT (Authentication context).
 */
public interface DashboardService {

    /**
     * Fetch dashboard summary for logged-in user
     */
    DashboardSummaryResponse getSummaryByEmail(String email);

    /**
     * Fetch paginated transactions
     */
    Page<TransactionResponse> getTransactionsByEmail(String email, int page, int size);

    /**
     * Fetch paginated notifications
     */
    Page<NotificationResponse> getNotificationsByEmail(String email, int page, int size);

    /**
     * Fetch paginated activity logs
     */
    Page<ActivityResponse> getActivitiesByEmail(String email, int page, int size);
}