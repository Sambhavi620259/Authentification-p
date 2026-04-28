package in.bawvpl.Authify.io;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteResponse {

    private Long appId;
    private String appName;
    private String appLogo;
    private String appUrl;
}