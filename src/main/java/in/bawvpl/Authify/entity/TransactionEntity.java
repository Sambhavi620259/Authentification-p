package in.bawvpl.Authify.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ RELATION WITH USER
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    // ✅ RELATION WITH APP
    @ManyToOne
    @JoinColumn(name = "app_id")
    private AppEntity app;

    // ✅ TABLE-4 FIELDS
    private String paymentDescription;

    private LocalDateTime paymentDate;

    private String paymentMethod;

    private String paymentSource;

    private Double amount;

    private String paymentStatus;

    // ✅ AUTO TIME
    @PrePersist
    public void onCreate() {
        this.paymentDate = LocalDateTime.now();
        if (this.paymentStatus == null) {
            this.paymentStatus = "In Process";
        }
    }
}