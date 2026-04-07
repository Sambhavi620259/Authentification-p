package in.bawvpl.Authify.entity;

import lombok.*;
import jakarta.persistence.*;

@Entity
@Table(name = "subscription_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String planId;

    private String name;
    private String description;
    private double price;
    private String billingCycle; // e.g. "MONTHLY", "YEARLY"
}
