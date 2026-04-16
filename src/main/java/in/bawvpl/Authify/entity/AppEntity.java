package in.bawvpl.Authify.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppEntity {

    // ================= ID =================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long appId;

    // ================= TYPE =================
    @Column(name = "app_type", nullable = false)
    private String appType;

    // ================= NAME =================
    @Column(name = "app_name", nullable = false, length = 150)
    private String appName;

    // ================= DESCRIPTION =================
    @Column(name = "app_text", length = 1000)
    private String appText;

    // ================= URL =================
    @Column(name = "app_url", nullable = false)
    private String appUrl;

    // ================= LOGO =================
    @Column(name = "app_logo")
    private String appLogo;

    // ================= STATUS =================
    @Builder.Default
    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";

    // ================= TIMESTAMP =================
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ================= AUTO =================
    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null || status.isBlank()) {
            status = "ACTIVE";
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}