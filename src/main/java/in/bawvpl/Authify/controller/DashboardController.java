package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.io.TransactionResponse;
import in.bawvpl.Authify.io.DashboardSummaryResponse;
import in.bawvpl.Authify.service.DashboardService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1.0/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // ✅ Dashboard Summary
    @GetMapping("/summary/{userId}")
    public ResponseEntity<DashboardSummaryResponse> getSummary(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                dashboardService.getSummary(userId)
        );
    }

    // ✅ Transactions with pagination
    @GetMapping("/transactions/{userId}")
    public ResponseEntity<Page<TransactionResponse>> getTransactions(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                dashboardService.getTransactions(userId, page, size)
        );
    }
}