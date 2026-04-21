package in.bawvpl.Authify.io;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private String title;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
}
