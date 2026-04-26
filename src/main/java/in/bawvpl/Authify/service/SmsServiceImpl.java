package in.bawvpl.Authify.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsServiceImpl implements SmsService {

    // 🔥 Toggle SMS ON/OFF from properties
    @Value("${app.sms.enabled:false}")
    private boolean smsEnabled;

    // ================= VERIFY OTP =================
    @Override
    public void sendVerificationOtp(String phoneNumber, String otp) {

        if (!smsEnabled) {
            log.info("📵 SMS disabled (verification OTP not sent)");
            return;
        }

        if (!isValid(phoneNumber, otp)) return;

        String formattedPhone = formatPhone(phoneNumber);

        log.info("[SMS] Sending verification OTP to {}", formattedPhone);

        // 👉 Future integration
        // sendSms(formattedPhone, "Your verification OTP is: " + otp);
    }

    // ================= RESET OTP =================
    @Override
    public void sendResetOtp(String phoneNumber, String otp) {

        if (!smsEnabled) {
            log.info("📵 SMS disabled (reset OTP not sent)");
            return;
        }

        if (!isValid(phoneNumber, otp)) return;

        String formattedPhone = formatPhone(phoneNumber);

        log.info("[SMS] Sending reset OTP to {}", formattedPhone);

        // 👉 Future integration
        // sendSms(formattedPhone, "Your reset OTP is: " + otp);
    }

    // ================= VALIDATION =================
    private boolean isValid(String phoneNumber, String otp) {

        if (phoneNumber == null || phoneNumber.isBlank()) {
            log.warn("⚠️ Phone number missing, SMS skipped");
            return false;
        }

        if (otp == null || otp.isBlank()) {
            log.warn("⚠️ OTP missing, SMS skipped");
            return false;
        }

        return true;
    }

    // ================= FORMAT PHONE =================
    private String formatPhone(String phoneNumber) {

        String clean = phoneNumber.replaceAll("\\D", "");

        if (clean.length() == 10) {
            return "+91" + clean;
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