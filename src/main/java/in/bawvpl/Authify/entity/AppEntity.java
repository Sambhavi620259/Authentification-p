package in.bawvpl.Authify.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppEntity {

    // ✅ App_ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long appId;

    // ✅ App_Type (B2B / B2C)
    private String appType;

    // ✅ App_Name
    private String appName;

    // ✅ App_Text
    @Column(length = 1000)
    private String appText;

    // ✅ App_URL
    private String appUrl;

    // ✅ App_Logo
    private String appLogo;
}