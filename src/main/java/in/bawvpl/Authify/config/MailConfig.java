package in.bawvpl.Authify.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();

        // ✅ AWS SES SMTP (change region if needed)
        sender.setHost("email-smtp.ap-south-1.amazonaws.com");
        sender.setPort(587);

        // ✅ Your SES SMTP credentials
        sender.setUsername("AKIAX3LRHIJIIUVZRN6C");
        sender.setPassword("BPo79hn3RMQw5f/IJMegSqTfgpCD9ALtZp6azk1ci9Sy");

        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");

        return sender;
    }
}