package in.bawvpl.Authify.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_verification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= USER =================
    @Column(name = "user_id")
    private Long userId;

    @Column(length = 150)
    private String email;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    // ================= OTP =================
    @Column(length = 10, nullable = false)
    private String otp;

    @Column(length = 20, nullable = false)
    private String purpose;

    // ================= SECURITY =================
    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;

    @Builder.Default
    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false;

    // 🔥 NEW FIELDS (FIX YOUR ERROR)
    @Builder.Default
    @Column(name = "attempts", nullable = false)
    private Integer attempts = 0;

    @Column(name = "last_sent_at")
    private LocalDateTime lastSentAt;

    // ================= TIMESTAMP =================
    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // ================= AUTO =================
    @PrePersist
    public void prePersist() {

        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }

        if (this.attempts == null) {
            this.attempts = 0;
        }

        if (this.isUsed == null) {
            this.isUsed = false;
        }
    }
}