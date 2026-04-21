package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.io.*;
import in.bawvpl.Authify.service.DashboardService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1.0/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // ================= SUMMARY =================
    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(Authentication auth) {

        String email = auth.getName();

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
    public ResponseEntity<?> getTransactions(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String email = auth.getName();

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