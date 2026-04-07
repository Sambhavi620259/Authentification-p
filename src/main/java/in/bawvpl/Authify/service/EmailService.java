package in.bawvpl.Authify.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:no-reply@authify.com}")
    private String from;

    private void sendOtpEmail(String to, String subject, String otp) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, "utf-8");

            String html = """
                    <h2>Your OTP</h2>
                    <h1>%s</h1>
                    """.formatted(otp);

            helper.setTo(to);
            helper.setFrom(from);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(msg);
            log.info("OTP sent to {}", to);

        } catch (Exception e) {
            log.error("Email failed", e);
            throw new RuntimeException("Email failed");
        }
    }

    public void sendVerificationOtpEmail(String to, String otp) {
        sendOtpEmail(to, "Verification OTP", otp);
    }

    public void sendResetOtpEmail(String to, String otp) {
        sendOtpEmail(to, "Reset OTP", otp);
    }
}