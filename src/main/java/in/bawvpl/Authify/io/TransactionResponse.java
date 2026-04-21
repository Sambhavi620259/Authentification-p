package in.bawvpl.Authify.io;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {
    private Double amount;
    private String status;
    private String type;
    private LocalDateTime date;
}