package in.bawvpl.Authify.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Settings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= USER =================
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    // ================= SETTINGS =================
    @Builder.Default
    @Column(name = "notifications_enabled")
    private Boolean notificationsEnabled = true;

    @Builder.Default
    @Column(name = "email_alerts")
    private Boolean emailAlerts = true;

    @Builder.Default
    @Column(name = "dark_mode")
    private Boolean darkMode = false;
}