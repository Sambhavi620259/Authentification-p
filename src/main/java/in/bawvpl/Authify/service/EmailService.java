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
                <div style="font-family:Arial, sans-serif;">
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
                <div style="font-family: Arial, sans-serif; line-height:1.6;">

                    <h2>Email Verification</h2>

                    <p>Thank you for registering with us.</p>

                    <p>Please click the button below to verify your email:</p>

                    <!-- BUTTON -->
                    <a href="%s" style="
                        display:inline-block;
                        padding:12px 24px;
                        background-color:#2e6cff;
                        color:white;
                        text-decoration:none;
                        border-radius:6px;
                        font-weight:500;
                        margin-top:10px;
                    ">
                        Verify Email
                    </a>

                    <br/><br/>

                    <!-- BACKUP LINK -->
                    <p>If the button does not work, click the link below:</p>

                    <p>
                        <a href="%s" style="color:#2e6cff; word-break: break-all;">
                            %s
                        </a>
                    </p>

                    <br/>

                    <p style="color:#888;">
                        This link may expire after some time.
                    </p>

                    <p style="color:#888;">
                        If you did not register, please ignore this email.
                    </p>

                </div>
                """.formatted(verificationLink, verificationLink, verificationLink);

        sendHtmlEmail(to, "Verify Your Email", html);
    }
}