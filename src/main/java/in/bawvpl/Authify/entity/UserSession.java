package in.bawvpl.Authify.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions",
        indexes = {
                @Index(name = "idx_session_user", columnList = "user_id"),
                @Index(name = "idx_session_token", columnList = "token")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= USER =================
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // ================= TOKEN =================
    @Column(nullable = false, length = 500)
    private String token;

    // ================= DEVICE INFO =================
    @Column(length = 50)
    private String ip;

    @Column(length = 200)
    private String device;

    // ================= STATUS =================
    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    // ================= TIMESTAMP =================
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (active == null) {
            active = true;
        }
    }
}