package in.bawvpl.Authify.io;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApplicationResponse {

    private Long appId;
    private String appName;
    private String appType;
    private String appUrl;
    private String appLogo;
    private String status;
}