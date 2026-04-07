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
public class OtpVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String email;
    private String phoneNumber;

    private String otp;
    private String purpose;

    private LocalDateTime expiryTime;
    private Boolean isUsed;

    private LocalDateTime createdAt = LocalDateTime.now();
}