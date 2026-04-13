package in.bawvpl.Authify.io;

import lombok.Data;

@Data
public class ProfileRequest {

    private String name;

    private String email;

    private String phoneNumber;

    private String password;

    // ✅ NEW FIELDS (YOU WERE MISSING THESE)

    private String address;

    private String referralCode;

    private String documentType;      // AADHAAR / PAN

    private String documentNumber;    // Aadhaar / PAN number
}