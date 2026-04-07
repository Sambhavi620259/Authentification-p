package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.TransactionEntity;
import in.bawvpl.Authify.io.DashboardSummaryResponse;
import in.bawvpl.Authify.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1.0/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary/{userId}")
    public ResponseEntity<DashboardSummaryResponse> getSummary(@PathVariable String userId) {
        DashboardSummaryResponse summary = dashboardService.getSummary(userId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/transactions/{userId}")
    public ResponseEntity<List<TransactionEntity>> getRecentTransactions(
            @PathVariable String userId,
            @RequestParam(name = "limit", defaultValue = "10") int limit) {
        List<TransactionEntity> txns = dashboardService.getRecentTransactions(userId, limit);
        return ResponseEntity.ok(txns);
    }
}
