package in.bawvpl.Authify.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "activity_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= USER RELATION =================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private UserEntity user;

    // ================= ACTION =================
    @Column(nullable = false)
    private String action; // LOGIN, PAYMENT_CREATED, etc.

    // ================= DESCRIPTION =================
    @Column(name = "description")
    private String description;

    // ================= TIMESTAMP =================
    @Column(nullable = false, updatable = false)
    private Instant timestamp;

    // ================= AUTO =================
    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = Instant.now();
        }
    }
}