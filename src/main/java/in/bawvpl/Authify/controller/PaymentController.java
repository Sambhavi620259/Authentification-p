package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.TransactionEntity;
import in.bawvpl.Authify.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1.0/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // ✅ CREATE PAYMENT
    @PostMapping
    public TransactionEntity create(
            @RequestParam Long userId,
            @RequestParam Long appId,
            @RequestParam String method,
            @RequestParam String source,
            @RequestParam Double amount
    ) {
        return paymentService.createPayment(userId, appId, method, source, amount);
    }

    // ✅ GET USER PAYMENTS
    @GetMapping("/user/{userId}")
    public List<TransactionEntity> userPayments(@PathVariable Long userId) {
        return paymentService.getUserPayments(userId);
    }

    // ✅ UPDATE STATUS
    @PutMapping("/{id}")
    public TransactionEntity update(
            @PathVariable Long id,
            @RequestParam String status
    ) {
        return paymentService.updateStatus(id, status);
    }
}