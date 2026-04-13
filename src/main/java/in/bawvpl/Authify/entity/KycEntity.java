package in.bawvpl.Authify.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "kyc_entity")
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
    private String documentType; // PAN / AADHAAR / DL / VOTER_ID

    @Column(name = "document_number", nullable = false, length = 50)
    private String documentNumber; // Aadhaar / PAN number

    @Column(name = "file_path", nullable = false)
    private String filePath; // uploaded file name

    // ================= KYC STATUS =================

    @Column(nullable = false)
    private String status; // PENDING / VERIFIED / REJECTED

    @Column(nullable = false)
    private Boolean completed; // use Boolean (safer than primitive)

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

        // Timestamp
        if (this.uploadedAt == null) {
            this.uploadedAt = Instant.now();
        }

        // Default status
        if (this.status == null || this.status.isBlank()) {
            this.status = "PENDING"; // ✅ correct flow
        }

        // Default completion
        if (this.completed == null) {
            this.completed = false;
        }
    }
}