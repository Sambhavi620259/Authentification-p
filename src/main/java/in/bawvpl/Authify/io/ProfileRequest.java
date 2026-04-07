package in.bawvpl.Authify.io;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProfileRequest {
    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    private String aadhaarNumber;
    private String panNumber;// optional but accepted
}
