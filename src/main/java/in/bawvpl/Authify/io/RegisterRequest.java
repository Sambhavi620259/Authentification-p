package in.bawvpl.Authify.io;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    // ================= BASIC =================

    @NotBlank(message = "Entity type is required")
    private String entityType; // INDIVIDUAL / ADMIN

    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Invalid email")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @Pattern(regexp = "^[0-9]{10}$", message = "Invalid phone number")
    @NotBlank(message = "Mobile number is required")
    private String phoneNumber;

    // ================= NEW FIELDS =================

    @NotBlank(message = "Address is required")
    private String address;

    private String referralCode; // optional

    // ================= KYC =================

    @NotBlank(message = "Document type is required")
    private String documentType; // AADHAAR / PAN / DL / VOTER_ID

    @NotBlank(message = "Document number is required")
    private String documentNumber;

    // ================= CUSTOM VALIDATION =================

    @AssertTrue(message = "Invalid document number format")
    public boolean isDocumentValid() {

        if (documentType == null || documentNumber == null) {
            return false;
        }

        switch (documentType.toUpperCase()) {

            case "AADHAAR":
                return documentNumber.matches("\\d{12}");

            case "PAN":
                return documentNumber.matches("[A-Z]{5}[0-9]{4}[A-Z]");

            case "DL":
            case "VOTER_ID":
                return true; // optional validation

            default:
                return false;
        }
    }
}