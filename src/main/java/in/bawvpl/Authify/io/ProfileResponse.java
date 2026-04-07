package in.bawvpl.Authify.io;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileResponse {

    private String userId;

    private String name; // entityName

    private String email;

    private String phoneNumber; // mobile

    private Boolean isAccountVerified; // emailVerified

    private Boolean isKycVerified;
}