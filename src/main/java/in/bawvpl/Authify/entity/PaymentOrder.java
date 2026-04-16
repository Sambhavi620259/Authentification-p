package in.bawvpl.Authify.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "payment_orders",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "order_id") // 🔥 important
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= USER =================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private UserEntity user;

    // ================= APP =================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_id")
    @JsonIgnore
    private AppEntity app;

    // ================= PAYMENT =================
    @Column(name = "order_id", nullable = false, unique = true, length = 100)
    private String orderId;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod; // CARD / UPI / NETBANKING

    @Column(name = "payment_status", nullable = false)
    private String paymentStatus; // CREATED / PENDING / SUCCESS / FAILED

    @Column(nullable = false)
    private Double amount;

    // ================= TIMESTAMP =================
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ================= AUTO =================
    @PrePersist
    public void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        updatedAt = LocalDateTime.now();

        if (paymentStatus == null || paymentStatus.isBlank()) {
            paymentStatus = "CREATED";
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}