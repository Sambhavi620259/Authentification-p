package in.bawvpl.Authify.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "applications",
        indexes = {
                @Index(name = "idx_app_user", columnList = "user_id"),
                @Index(name = "idx_app_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user"}) // ✅ avoid lazy loading / recursion issues
public class ApplicationEntity {

    // ================= ID =================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "app_id")
    private Long appId;

    // ================= USER =================
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private UserEntity user;

    // ================= APP DETAILS =================
    @Column(name = "app_type", nullable = false, length = 50)
    private String appType;

    @Column(name = "app_name", nullable = false, length = 150)
    private String appName;

    @Column(name = "app_text", length = 500)
    private String appText;

    @Column(name = "app_url", nullable = false, length = 500)
    private String appUrl;

    @Column(name = "app_logo", length = 500)
    private String appLogo;

    // ================= STATUS =================
    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE"; // ACTIVE / DELETED / INACTIVE

    // ================= TIMESTAMP =================
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ================= LIFECYCLE =================
    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        if (this.createdAt == null) {
            this.createdAt = now;
        }

        if (this.status == null || this.status.isBlank()) {
            this.status = "ACTIVE";
        }

        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}