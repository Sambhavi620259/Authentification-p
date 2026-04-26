package in.bawvpl.Authify.io;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class RecentAppDto {

    private Long appId;
    private String appName;
    private String appLogo;
    private String appUrl;
    private Integer visitCount;
    private LocalDateTime lastUsed;
}
