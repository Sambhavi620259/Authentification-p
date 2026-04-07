package in.bawvpl.Authify.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    // ✅ PRIMARY KEY
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ BUSINESS ID
    @Column(unique = true, nullable = false)
    private String userId;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    // BASIC
    private String entityType;
    private String entityName;
    private String contactPerson;
    private String entityPic;

    private String mobile;
    private String address;

    // VERIFICATION
    private Boolean emailVerified = false;

    // PASSWORD TRACKING
    private LocalDateTime passDate;
    private String passStatus;

    // KYC
    private String idType;
    private String idProof;
    private String idDoc;

    // REFERRAL
    private Long referredBy;
    private String referralLink;

    // ADMIN
    private String userStatus;
    private String adminRole;

    // TERMS
    @Column(length = 2000)
    private String tncText;

    // TIME
    private LocalDateTime creationTime;
    private LocalDateTime lastUpdate;

    // OTP
    private String verifyOtp;
    private Long verifyOtpExpireAt;

    private String resetOtp;
    private Long resetOtpExpireAt;

    private String registerOtp;
    private Long registerOtpExpireAt;

    // RELATION
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private KycEntity kyc;

    // AUTO TIMESTAMP
    @PrePersist
    public void onCreate() {
        this.creationTime = LocalDateTime.now();
        this.lastUpdate = LocalDateTime.now();
        this.passDate = LocalDateTime.now();
        this.passStatus = "Active";
        this.userStatus = "Active";
        this.emailVerified = false;

        if (this.referredBy == null) {
            this.referredBy = 1000000L;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.lastUpdate = LocalDateTime.now();
    }
}