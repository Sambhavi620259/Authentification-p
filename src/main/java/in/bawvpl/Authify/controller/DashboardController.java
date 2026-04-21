package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.io.*;
import in.bawvpl.Authify.service.DashboardService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1.0/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    // ================= SUMMARY =================
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary(Authentication auth) {

        if (auth == null || auth.getName() == null) {
            throw new RuntimeException("Unauthorized");
        }

        String email = auth.getName();

        log.info("Fetching dashboard summary for {}", email);

        DashboardSummaryResponse response =
                dashboardService.getSummaryByEmail(email);

        return ResponseEntity.ok(
                ApiResponse.<DashboardSummaryResponse>builder()
                        .status(200)
                        .message("Dashboard summary fetched")
                        .data(response)
                        .build()
        );
    }

    // ================= TRANSACTIONS =================
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactions(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (auth == null || auth.getName() == null) {
            throw new RuntimeException("Unauthorized");
        }

        String email = auth.getName();

        log.info("Fetching transactions for {} | page={} size={}", email, page, size);

        Page<TransactionResponse> response =
                dashboardService.getTransactionsByEmail(email, page, size);

        return ResponseEntity.ok(
                ApiResponse.<Page<TransactionResponse>>builder()
                        .status(200)
                        .message("Transactions fetched")
                        .data(response)
                        .build()
        );
    }
}