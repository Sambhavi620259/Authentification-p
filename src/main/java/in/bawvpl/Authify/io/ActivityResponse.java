package in.bawvpl.Authify.io;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class ActivityResponse {

    private String action;
    private String description;
    private Instant timestamp;
}