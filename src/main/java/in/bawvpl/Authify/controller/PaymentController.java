package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.TransactionEntity;
import in.bawvpl.Authify.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1.0/payment")
@RequiredArgsConstructor
@CrossOrigin("*")
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    // ================= CREATE PAYMENT =================
    @PostMapping
    public ResponseEntity<?> createPayment(
            Authentication authentication,
            @RequestParam Long appId,
            @RequestParam String method,
            @RequestParam String source,
            @RequestParam Double amount
    ) {

        try {
            String email = authentication.getName().toLowerCase().trim();

            TransactionEntity txn = paymentService.createPayment(
                    email, appId, method, source, amount
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Payment created",
                    "data", txn
            ));

        } catch (Exception e) {
            log.error("Payment create error: {}", e.getMessage());

            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage()
            ));
        }
    }

    // ================= GET USER PAYMENTS =================
    @GetMapping("/my")
    public ResponseEntity<?> userPayments(Authentication authentication) {

        try {
            String email = authentication.getName().toLowerCase().trim();

            List<TransactionEntity> list =
                    paymentService.getUserPayments(email);

            return ResponseEntity.ok(Map.of(
                    "message", "Payments fetched",
                    "data", list
            ));

        } catch (Exception e) {
            log.error("Fetch payments error: {}", e.getMessage());

            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage()
            ));
        }
    }

    // ================= UPDATE STATUS =================
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestParam String status
    ) {

        try {
            TransactionEntity txn =
                    paymentService.updateStatus(id, status);

            return ResponseEntity.ok(Map.of(
                    "message", "Payment updated",
                    "data", txn
            ));

        } catch (Exception e) {
            log.error("Update payment error: {}", e.getMessage());

            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage()
            ));
        }
    }
}