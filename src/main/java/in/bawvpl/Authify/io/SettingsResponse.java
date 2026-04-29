package in.bawvpl.Authify.io;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SettingsResponse {

    private Boolean notificationsEnabled;
    private Boolean emailAlerts;
    private Boolean darkMode;
}