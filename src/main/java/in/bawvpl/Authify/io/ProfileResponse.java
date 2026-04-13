package in.bawvpl.Authify.io;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileResponse {

    // ================= BASIC =================

    private String userId;

    private String name; // entityName

    private String email;

    private String phoneNumber;

    private Boolean isAccountVerified; // emailVerified

    private Boolean isKycVerified;

    // ================= EXTRA =================

    private String referralCode;

    private String documentType;

    private String documentNumber;

    private String kycStatus;

    private String filePath;

    // ================= OPTIONAL (RECOMMENDED) =================

    private String photoUrl; // profile image
}