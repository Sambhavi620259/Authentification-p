package in.bawvpl.Authify.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_applications",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "app_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= USER =================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore // 🔥 prevents infinite JSON loop
    private UserEntity user;

    // ================= APPLICATION =================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_id", nullable = false)
    @ToString.Exclude
    private AppEntity app;

    // ================= VISIT COUNT =================
    @Builder.Default
    @Column(name = "visit_counter")
    private Integer visitCounter = 0;

    // ================= SUBSCRIPTION STATUS =================
    @Builder.Default
    @Column(name = "subscription_status")
    private String subscriptionStatus = "APPLIED";
    // APPLIED / ACTIVE / EXPIRED

    // ================= TIMESTAMP =================
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ================= AUTO DEFAULT =================
    @PrePersist
    public void prePersist() {

        if (visitCounter == null) {
            visitCounter = 0;
        }

        if (subscriptionStatus == null) {
            subscriptionStatus = "APPLIED";
        }

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}