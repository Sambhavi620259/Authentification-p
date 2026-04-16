package in.bawvpl.Authify.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "kyc",
        indexes = {
                @Index(name = "idx_kyc_user", columnList = "user_id")
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

    @Column(name = "document_type", nullable = false)
    private String documentType; // PAN / AADHAAR

    @Column(name = "document_number", nullable = false, length = 50)
    private String documentNumber;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    // ================= STATUS =================

    @Column(name = "status", nullable = false)
    private String status; // PENDING / VERIFIED / REJECTED

    @Column(name = "completed", nullable = false)
    private Boolean completed;

    // ================= TIMESTAMP =================

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private Instant uploadedAt;

    // ================= RELATION =================

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    // ================= AUTO INIT =================

    @PrePersist
    protected void onCreate() {

        if (this.uploadedAt == null) {
            this.uploadedAt = Instant.now();
        }

        if (this.status == null || this.status.isBlank()) {
            this.status = "PENDING";
        }

        if (this.completed == null) {
            this.completed = false;
        }
    }

    // ================= HELPER =================

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