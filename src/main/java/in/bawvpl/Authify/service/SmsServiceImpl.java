package in.bawvpl.Authify.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsServiceImpl implements SmsService {

    @Override
    public void sendVerificationOtp(String phoneNumber, String otp) {
        // TEMP implementation — replace later with SNS or Twilio
        log.info("[SMS] Sending verification OTP {} to {}", otp, phoneNumber);
    }

    @Override
    public void sendResetOtp(String phoneNumber, String otp) {
        // TEMP implementation — replace later with SNS or Twilio
        log.info("[SMS] Sending reset OTP {} to {}", otp, phoneNumber);
    }
}
