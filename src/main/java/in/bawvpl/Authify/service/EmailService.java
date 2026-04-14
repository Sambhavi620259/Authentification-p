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

    // ================= COMMON EMAIL SENDER =================
    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "utf-8");

            helper.setTo(to);
            helper.setFrom(from);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(msg);

            log.info("Email sent to {}", to);

        } catch (Exception e) {
            log.error("Email failed", e);
            throw new RuntimeException("Email sending failed");
        }
    }

    // ================= OTP EMAIL =================
    private void sendOtpEmail(String to, String subject, String otp) {

        String html = """
                <div style="font-family:Arial;">
                    <h2>Your OTP Code</h2>
                    <h1 style="color:#2e6cff;">%s</h1>
                    <p>This OTP is valid for a limited time.</p>
                </div>
                """.formatted(otp);

        sendHtmlEmail(to, subject, html);
    }

    public void sendVerificationOtpEmail(String to, String otp) {
        sendOtpEmail(to, "Verification OTP", otp);
    }

    public void sendResetOtpEmail(String to, String otp) {
        sendOtpEmail(to, "Reset Password OTP", otp);
    }

    // ================= EMAIL VERIFICATION LINK =================
    public void sendVerificationEmail(String to, String verificationLink) {

        String html = """
                <div style="font-family:Arial;">
                    <h2>Email Verification</h2>
                    <p>Thank you for registering.</p>
                    <p>Please click the button below to verify your email:</p>
                    
                    <a href="%s" style="
                        display:inline-block;
                        padding:10px 20px;
                        background-color:#2e6cff;
                        color:white;
                        text-decoration:none;
                        border-radius:5px;
                    ">
                        Verify Email
                    </a>

                    <p>If you did not register, please ignore this email.</p>
                </div>
                """.formatted(verificationLink);

        sendHtmlEmail(to, "Verify Your Email", html);
    }
}