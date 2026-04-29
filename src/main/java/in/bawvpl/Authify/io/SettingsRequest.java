package in.bawvpl.Authify.io;

import lombok.Data;

@Data
public class SettingsRequest {

    private Boolean notificationsEnabled;
    private Boolean emailAlerts;
    private Boolean darkMode;
}