package in.bawvpl.Authify.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "kyc",
        indexes = {
                @Index(name = "idx_kyc_user", columnList = "user_id"),
                @Index(name = "idx_kyc_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= DOCUMENT =================
    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType;

    @Column(name = "document_number", nullable = false, length = 50)
    private String documentNumber;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    // ================= STATUS =================
    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING"; // PENDING / VERIFIED / REJECTED

    @Builder.Default
    @Column(name = "completed", nullable = false)
    private Boolean completed = false;

    // ================= REJECTION =================
    @Column(name = "rejection_reason", length = 255)
    private String rejectionReason;

    // ================= TIMESTAMPS =================
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private Instant uploadedAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // ================= RELATION =================
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnore
    private UserEntity user;

    // ================= AUTO =================
    @PrePersist
    protected void onCreate() {

        Instant now = Instant.now();

        if (this.uploadedAt == null) {
            this.uploadedAt = now;
        }

        this.updatedAt = now;

        if (this.status == null || this.status.isBlank()) {
            this.status = "PENDING";
        }

        if (this.completed == null) {
            this.completed = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // ================= HELPERS =================
    public boolean isVerified() {
        return "VERIFIED".equalsIgnoreCase(this.status);
    }

    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(this.status);
    }

    public boolean isRejected() {
        return "REJECTED".equalsIgnoreCase(this.status);
    }
}