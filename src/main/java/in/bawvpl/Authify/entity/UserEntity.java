package in.bawvpl.Authify.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_user_email", columnList = "email"),
                @Index(name = "idx_user_userid", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= UNIQUE USER ID =================
    @Column(name = "user_id", unique = true, nullable = false, updatable = false)
    private String userId;

    // ================= BASIC DETAILS =================
    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "entity_name")
    private String entityName;

    @Column(name = "contact_person")
    private String contactPerson;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @Column(nullable = false)
    private String password;

    // ================= ROLE =================
    @Column(name = "admin_role", nullable = false)
    private String role;

    // ================= USER STATUS =================
    @Builder.Default
    @Column(name = "user_status", nullable = false)
    private String userStatus = "ACTIVE";

    // ================= EMAIL VERIFICATION =================
    @Builder.Default
    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Column(name = "verification_token")
    private String verificationToken;

    // ================= KYC =================
    @Builder.Default
    @Column(name = "is_kyc_verified", nullable = false)
    private Boolean isKycVerified = false;

    // ================= ADDRESS =================
    private String address;

    // ================= REFERRAL =================
    @Column(name = "referral_code", unique = true, nullable = false)
    private String referralCode;

    @Column(name = "referred_by")
    private String referredBy;

    // ================= PROFILE IMAGE =================
    @Column(name = "photo_url")
    private String photoUrl;

    // ================= TIMESTAMPS =================
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ================= AUTO =================
    @PrePersist
    public void prePersist() {

        LocalDateTime now = LocalDateTime.now();

        // ✅ Generate userId if missing
        if (this.userId == null || this.userId.isBlank()) {
            this.userId = generateUserId();
        }

        // ✅ timestamps
        if (this.createdAt == null) {
            this.createdAt = now;
        }

        this.updatedAt = now;

        // ✅ Normalize email
        if (this.email != null) {
            this.email = this.email.toLowerCase().trim();
        }

        // ✅ Defaults
        if (this.userStatus == null) {
            this.userStatus = "ACTIVE";
        }

        if (this.emailVerified == null) {
            this.emailVerified = false;
        }

        if (this.isKycVerified == null) {
            this.isKycVerified = false;
        }

        if (this.role == null || this.role.isBlank()) {
            this.role = "ROLE_USER";
        }

        // ✅ Referral Code
        if (this.referralCode == null || this.referralCode.isBlank()) {
            this.referralCode = generateReferralCode();
        }
    }

    @PreUpdate
    public void preUpdate() {

        this.updatedAt = LocalDateTime.now();

        if (this.email != null) {
            this.email = this.email.toLowerCase().trim();
        }
    }

    @Column(name = "reset_otp")
    private String resetOtp;

    @Column(name = "reset_otp_expiry")
    private Instant resetOtpExpiry;

    // ================= HELPERS =================

    private String generateUserId() {
        return "USR" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateReferralCode() {
        return "REF" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}