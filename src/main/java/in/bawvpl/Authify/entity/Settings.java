package in.bawvpl.Authify.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "settings",
        indexes = {
                @Index(name = "idx_settings_user", columnList = "user_id")
        }
)
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
    @JsonIgnore // 🔥 prevents infinite loop
    private UserEntity user;

    // ================= SETTINGS =================
    @Builder.Default
    @Column(name = "notifications_enabled", nullable = false)
    private Boolean notificationsEnabled = true;

    @Builder.Default
    @Column(name = "email_alerts", nullable = false)
    private Boolean emailAlerts = true;

    @Builder.Default
    @Column(name = "dark_mode", nullable = false)
    private Boolean darkMode = false;

    // ================= TIMESTAMPS =================
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ================= AUTO =================
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (this.createdAt == null) {
            this.createdAt = now;
        }

        this.updatedAt = now;

        // null safety
        if (this.notificationsEnabled == null) this.notificationsEnabled = true;
        if (this.emailAlerts == null) this.emailAlerts = true;
        if (this.darkMode == null) this.darkMode = false;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}