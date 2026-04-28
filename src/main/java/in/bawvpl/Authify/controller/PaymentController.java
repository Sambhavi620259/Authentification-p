package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.PaymentOrder;
import in.bawvpl.Authify.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1.0/payment")
@RequiredArgsConstructor
@CrossOrigin("*")
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    // ================= CREATE ORDER =================
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(
            Authentication authentication,
            @RequestParam Long appId,
            @RequestParam Double amount
    ) {

        String email = authentication.getName().toLowerCase().trim();

        PaymentOrder order =
                paymentService.createOrder(email, appId, amount);

        return ResponseEntity.ok(Map.of(
                "orderId", order.getOrderId(),
                "amount", order.getAmount(),
                "upiId", "yourupi@bank"   // 🔥 change this
        ));
    }

    // ================= VERIFY PAYMENT =================
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            @RequestParam String orderId,
            @RequestParam String status
    ) {

        return ResponseEntity.ok(
                paymentService.verifyPayment(orderId, status)
        );
    }
}