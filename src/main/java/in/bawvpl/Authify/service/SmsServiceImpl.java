package in.bawvpl.Authify.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import jakarta.annotation.PostConstruct;

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

    // ================= INIT (ONLY ONCE) =================
    @PostConstruct
    public void init() {
        if (smsEnabled && !accountSid.isBlank() && !authToken.isBlank()) {
            Twilio.init(accountSid, authToken);
            log.info("✅ Twilio initialized");
        } else {
            log.warn("⚠️ Twilio not initialized (check config or disabled)");
        }
    }

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
            Message msg = Message.creator(
                    new PhoneNumber(formattedPhone),
                    new PhoneNumber(fromNumber),
                    message
            ).create();

            log.info("✅ SMS sent to {} | SID={}", formattedPhone, msg.getSid());

        } catch (Exception e) {

            log.error("❌ SMS failed for {}: {}", formattedPhone, e.getMessage(), e);

            // 🔥 optional: throw for API failure visibility
            throw new RuntimeException("SMS delivery failed");
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
            log.error("❌ Twilio config missing (SID/TOKEN/NUMBER)");
            return false;
        }

        return true;
    }

    // ================= FORMAT PHONE =================
    private String formatPhone(String phoneNumber) {

        String clean = phoneNumber.replaceAll("\\D", "");

        // 🇮🇳 India default
        if (clean.length() == 10) {
            return "+91" + clean;
        }

        // already contains country code (India)
        if (clean.startsWith("91") && clean.length() == 12) {
            return "+" + clean;
        }

        // already formatted
        if (phoneNumber.startsWith("+")) {
            return phoneNumber;
        }

        // fallback (assume international)
        return "+" + clean;
    }
}