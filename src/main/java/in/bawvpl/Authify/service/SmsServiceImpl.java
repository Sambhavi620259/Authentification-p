package in.bawvpl.Authify.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsServiceImpl implements SmsService {

    // 🔥 Enable/Disable SMS
    @Value("${app.sms.enabled:false}")
    private boolean smsEnabled;

    // 🔐 Twilio config
    @Value("${twilio.sid:}")
    private String accountSid;

    @Value("${twilio.token:}")
    private String authToken;

    @Value("${twilio.number:}")
    private String fromNumber;

    // ================= GENERIC SEND =================
    @Override
    public void sendSms(String phoneNumber, String message) {

        if (!smsEnabled) {
            log.info("📵 SMS disabled (skipped)");
            return;
        }

        if (!isValid(phoneNumber, message)) return;

        String formattedPhone = formatPhone(phoneNumber);

        try {
            Twilio.init(accountSid, authToken);

            Message.creator(
                    new com.twilio.type.PhoneNumber(formattedPhone),
                    new com.twilio.type.PhoneNumber(fromNumber),
                    message
            ).create();

            log.info("✅ SMS sent to {}", formattedPhone);

        } catch (Exception e) {
            log.error("❌ SMS failed: {}", e.getMessage(), e);
        }
    }

    // ================= VERIFY OTP =================
    @Override
    public void sendVerificationOtp(String phoneNumber, String otp) {
        sendSms(phoneNumber, "Your verification OTP is: " + otp);
    }

    // ================= RESET OTP =================
    @Override
    public void sendResetOtp(String phoneNumber, String otp) {
        sendSms(phoneNumber, "Your reset OTP is: " + otp);
    }

    // ================= VALIDATION =================
    private boolean isValid(String phoneNumber, String message) {

        if (phoneNumber == null || phoneNumber.isBlank()) {
            log.warn("⚠️ Phone number missing, SMS skipped");
            return false;
        }

        if (message == null || message.isBlank()) {
            log.warn("⚠️ Message empty, SMS skipped");
            return false;
        }

        if (accountSid.isBlank() || authToken.isBlank() || fromNumber.isBlank()) {
            log.warn("⚠️ Twilio config missing");
            return false;
        }

        return true;
    }

    // ================= FORMAT PHONE =================
    private String formatPhone(String phoneNumber) {

        String clean = phoneNumber.replaceAll("\\D", "");

        if (clean.length() == 10) {
            return "+91" + clean; // India default
        }

        if (clean.startsWith("91") && clean.length() == 12) {
            return "+" + clean;
        }

        if (phoneNumber.startsWith("+")) {
            return phoneNumber;
        }

        return "+" + clean;
    }
}