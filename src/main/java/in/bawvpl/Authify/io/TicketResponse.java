package in.bawvpl.Authify.io;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TicketResponse {

    private Long id;
    private String subject;
    private String status;
    private LocalDateTime createdAt;
}