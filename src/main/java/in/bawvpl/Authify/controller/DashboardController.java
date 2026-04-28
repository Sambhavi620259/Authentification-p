package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.io.*;
import in.bawvpl.Authify.service.DashboardService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1.0/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    // ================= HELPER =================
    private String getEmail(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return auth.getName();
    }

    // ================= MAIN DASHBOARD (🔥 IMPORTANT) =================
    @GetMapping
    public ResponseEntity<?> dashboard(Authentication auth) {

        String email = getEmail(auth);

        log.info("📊 Dashboard for {}", email);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("Dashboard fetched")
                        .data(dashboardService.getSummaryByEmail(email))
                        .build()
        );
    }

    // ================= SUMMARY =================
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary(Authentication auth) {

        String email = getEmail(auth);

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
            @RequestParam(defaultValue = "10") int size
    ) {

        String email = getEmail(auth);

        log.info("💰 Transactions for {} | page={} size={}", email, page, size);

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

    // ================= ACTIVITY (🔥 OPTIONAL BUT IMPORTANT) =================
    @GetMapping("/activity")
    public ResponseEntity<?> getActivity(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        String email = getEmail(auth);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("Activity fetched")
                        .data(dashboardService.getActivity(email, page, size))
                        .build()
        );
    }
}