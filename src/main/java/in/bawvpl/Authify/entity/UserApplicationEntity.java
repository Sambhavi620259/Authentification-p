package in.bawvpl.Authify.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserApplicationEntity {

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

    // ✅ VISIT COUNT
    private Integer visitCounter = 0;

    // ✅ SUBSCRIPTION STATUS
    private String subscriptionStatus = "Not Subscribed";
}