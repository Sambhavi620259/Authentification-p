package in.bawvpl.Authify.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
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
    private String entityType; // INDIVIDUAL / ORGANIZATION

    @Column(name = "entity_name")
    private String entityName;

    @Column(name = "contact_person")
    private String contactPerson;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(nullable = false)
    private String password;

    // ✅ FIXED (IMPORTANT)
    @Column(name = "admin_role")
    private String adminRole;

    // ================= USER STATUS =================
    @Builder.Default
    @Column(name = "user_status")
    private String userStatus = "ACTIVE";

    // ================= EMAIL VERIFICATION =================
    @Builder.Default
    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    @Column(name = "verification_token")
    private String verificationToken;

    // ================= KYC =================
    @Builder.Default
    @Column(name = "is_kyc_verified")
    private Boolean isKycVerified = false;

    // ================= ADDRESS =================
    private String address;

    // ================= REFERRAL =================
    @Column(name = "referral_code", unique = true)
    private String referralCode;

    @Column(name = "referred_by")
    private String referredBy;

    // ================= PROFILE IMAGE =================
    @Column(name = "photo_url")
    private String photoUrl;

    // ================= TIMESTAMPS =================
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ================= AUTO TIMESTAMPS =================
    @PrePersist
    public void prePersist() {

        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }

        this.updatedAt = LocalDateTime.now();

        if (this.userStatus == null) {
            this.userStatus = "ACTIVE";
        }

        if (this.emailVerified == null) {
            this.emailVerified = false;
        }

        if (this.isKycVerified == null) {
            this.isKycVerified = false;
        }

        // ✅ Generate referral if missing
        if (this.referralCode == null || this.referralCode.isEmpty()) {
            this.referralCode = generateReferralCode();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ================= HELPER =================
    private String generateReferralCode() {
        return "REF" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}