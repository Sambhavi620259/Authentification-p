package in.bawvpl.Authify.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "user_subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * User identifier (UUID or DB id depending on your app)
     */
    @Column(name = "user_id", nullable = false)
    private String userId;

    /**
     * Subscription plan id (string -> reference to subscription plan table)
     */
    @Column(name = "plan_id", nullable = false)
    private String planId;

    /**
     * Status: ACTIVE, CANCELLED, EXPIRED, etc.
     */
    @Column(name = "status")
    private String status;

    /**
     * Subscription start time (UTC)
     */
    @Column(name = "start_date")
    private Instant startDate;

    /**
     * Subscription end time (UTC). DashboardServiceImpl expects Instant and checks isAfter(Instant.now()).
     */
    @Column(name = "end_date")
    private Instant endDate;

    /**
     * When the subscription row was created
     */
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
