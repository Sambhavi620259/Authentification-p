package in.bawvpl.Authify.io;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    // ✅ TABLE-1 REQUIRED
    @NotBlank(message = "Entity type is required")
    private String entityType; // Individual / Organization

    @NotBlank(message = "Name is required")
    private String name; // Entity_Name

    @Email(message = "Invalid email")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @Pattern(regexp = "^[0-9]{10}$", message = "Invalid phone number")
    @NotBlank(message = "Mobile number is required")
    private String phoneNumber;

    // ================= OPTIONAL (KYC SUPPORT) =================

    @Pattern(regexp = "\\d{12}", message = "Invalid Aadhaar")
    private String aadhaarNumber;

    @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]{1}", message = "Invalid PAN")
    private String panNumber;
}