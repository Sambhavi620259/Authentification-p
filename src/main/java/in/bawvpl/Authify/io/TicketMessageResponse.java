package in.bawvpl.Authify.io;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TicketMessageResponse {

    private String sender;
    private String message;
    private LocalDateTime createdAt;
}