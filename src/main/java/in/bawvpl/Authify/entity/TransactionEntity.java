package in.bawvpl.Authify.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    // ================= USER =================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private UserEntity user;

    // ================= APP =================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_id")
    private ApplicationEntity app;

    // ================= DETAILS =================
    @Column(name = "payment_description")
    private String paymentDescription;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "payment_source")
    private String paymentSource;

    @Column(nullable = false)
    private Double amount;

    // ================= DASHBOARD =================
    @Column(name = "type", nullable = false)
    private String type; // CREDIT / DEBIT

    @Column(name = "status", nullable = false)
    private String status; // SUCCESS / FAILED / PENDING

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    // ================= AUTO =================
    @PrePersist
    public void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        if (this.paymentDate == null) {
            this.paymentDate = now;
        }

        if (this.status == null || this.status.isBlank()) {
            this.status = "PENDING";
        }

        if (this.type == null || this.type.isBlank()) {
            this.type = "DEBIT";
        }
    }
}