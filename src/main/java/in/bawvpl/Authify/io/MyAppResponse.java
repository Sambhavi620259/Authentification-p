package in.bawvpl.Authify.io;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MyAppResponse {

    private Long appId;
    private String appName;
    private String appLogo;
    private String appUrl;

    private Integer visitCounter;
    private String subscriptionStatus;

    private LocalDateTime updatedAt;
}