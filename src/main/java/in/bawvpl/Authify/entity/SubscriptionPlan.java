package in.bawvpl.Authify.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "subscription_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlan {

    // ================= ID =================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= PLAN ID =================
    @Column(name = "plan_id", nullable = false, unique = true, updatable = false)
    private String planId;

    // ================= DETAILS =================
    @Column(nullable = false)
    private String name;

    private String description;

    // ✅ FIX: use BigDecimal (money safe)
    @Column(nullable = false)
    private BigDecimal price;

    // ✅ BETTER CONTROL
    @Column(name = "billing_cycle", nullable = false)
    private String billingCycle; // MONTHLY, YEARLY

    // ================= STATUS =================
    @Builder.Default
    @Column(nullable = false)
    private String status = "ACTIVE";

    // ================= RELATION =================
    @OneToMany(mappedBy = "plan", fetch = FetchType.LAZY)
    private List<SubscriptionEntity> subscriptions;

    // ================= TIMESTAMPS =================
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ================= AUTO =================
    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;

        if (status == null || status.isBlank()) {
            status = "ACTIVE";
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}