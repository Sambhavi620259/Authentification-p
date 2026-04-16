package in.bawvpl.Authify.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "user_applications",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "app_id"}) // 🔥 prevent duplicates
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
    private UserEntity user;

    // ================= APPLICATION =================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_id", nullable = false)
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

    // ================= AUTO DEFAULT =================
    @PrePersist
    public void prePersist() {

        if (visitCounter == null) {
            visitCounter = 0;
        }

        if (subscriptionStatus == null) {
            subscriptionStatus = "APPLIED";
        }
    }
}