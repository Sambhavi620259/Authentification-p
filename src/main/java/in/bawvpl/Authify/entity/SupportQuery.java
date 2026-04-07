package in.bawvpl.Authify.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "support_queries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportQuery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ USER
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    // ✅ APPLICATION
    @ManyToOne
    @JoinColumn(name = "app_id")
    private AppEntity app;

    // ✅ ADMIN ID (simple for now)
    private Long attendedBy;

    // ✅ QUERY + ANSWER (same field)
    @Column(length = 2000)
    private String queryAnswer;

    // ✅ CREATED TIME
    private LocalDateTime queryCreated;

    // ✅ STATUS
    private String queryStatus; // Open / Closed

    @PrePersist
    public void onCreate() {
        this.queryCreated = LocalDateTime.now();
        if (this.queryStatus == null) {
            this.queryStatus = "Open";
        }
    }
}