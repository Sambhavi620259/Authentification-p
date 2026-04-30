package in.bawvpl.Authify.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs",
        indexes = {
                @Index(name = "idx_audit_user", columnList = "user_id"),
                @Index(name = "idx_audit_action", columnList = "action"),
                @Index(name = "idx_audit_time", columnList = "created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= USER =================
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // ================= ACTION =================
    @Column(nullable = false, length = 100)
    private String action;

    // ================= METADATA =================
    @Column(length = 500)
    private String metadata;

    // 🔥 ADD THESE (FIX YOUR ERROR)
    @Column(length = 50)
    private String ip;

    @Column(length = 200)
    private String device;

    // ================= TIMESTAMP =================
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}