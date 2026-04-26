package in.bawvpl.Authify.io;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private Long id; // ✅ REQUIRED

    private Double amount;
    private String type;
    private String status;
    private LocalDateTime paymentDate;

    private String paymentMethod;
    private String paymentSource;
    private String paymentDescription;
}