package in.bawvpl.Authify.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ RELATION WITH USER (IMPORTANT FIX)
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    // ✅ RELATION WITH APP (OPTIONAL BUT GOOD)
    @ManyToOne
    @JoinColumn(name = "app_id")
    private AppEntity app;

    // PAYMENT DETAILS
    private String orderId;
    private String paymentMethod;
    private String paymentStatus;
    private Double amount;

    private LocalDateTime createdAt;

    // AUTO TIMESTAMP
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.paymentStatus == null) {
            this.paymentStatus = "CREATED";
        }
    }
}