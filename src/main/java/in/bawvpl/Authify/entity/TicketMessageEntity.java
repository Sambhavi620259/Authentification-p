package in.bawvpl.Authify.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "ticket_messages",
        indexes = {
                @Index(name = "idx_ticket_msg_ticket", columnList = "ticket_id"),
                @Index(name = "idx_ticket_msg_created", columnList = "createdAt")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= RELATION =================
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    @JsonIgnore // 🔥 prevent infinite loop
    private TicketEntity ticket;

    // ================= DATA =================
    @Column(nullable = false, length = 20)
    private String sender; // USER / ADMIN

    @Column(nullable = false, length = 500)
    private String message;

    // ================= TIMESTAMP =================
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ================= AUTO =================
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}