package in.bawvpl.Authify.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_applications",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_app", columnNames = {"user_id", "app_id"})
        },
        indexes = {
                @Index(name = "idx_user_app", columnList = "user_id, app_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "app"}) // ✅ avoid recursion issues
public class UserApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= USER =================
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private UserEntity user;

    // ================= APPLICATION =================
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "app_id", nullable = false)
    @JsonIgnore
    private ApplicationEntity app;

    // ================= VISIT COUNT =================
    @Builder.Default
    @Column(name = "visit_counter", nullable = false)
    private Integer visitCounter = 0;

    // ================= SUBSCRIPTION STATUS =================
    @Builder.Default
    @Column(name = "subscription_status", nullable = false, length = 20)
    private String subscriptionStatus = "APPLIED";
    // APPLIED / ACTIVE / EXPIRED

    // ================= TIMESTAMP =================
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ================= AUTO =================
    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        if (visitCounter == null) {
            visitCounter = 0;
        }

        if (subscriptionStatus == null || subscriptionStatus.isBlank()) {
            subscriptionStatus = "APPLIED";
        }

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}