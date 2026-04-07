package in.bawvpl.Authify.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ IMPORTANT: use USER relation (already in your service)
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    private String aadhaarNumber;
    private String panNumber;
    private String status;

    private Boolean completed;
    private Instant uploadedAt;
}