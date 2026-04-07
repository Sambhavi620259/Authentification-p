package in.bawvpl.Authify.entity;

import lombok.*;
import jakarta.persistence.*;
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
    @Column(name = "id")
    private Long id;

    // user identifier (UUID or email)
    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String action; // e.g. "LOGIN", "PAYMENT_CREATED"

    @Column(name = "details")
    private String details;

    @Column(nullable = false)
    private Instant timestamp;
}
