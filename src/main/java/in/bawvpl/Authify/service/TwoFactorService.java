package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.time.SystemTimeProvider;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TwoFactorService {

    private final UserRepository userRepository;

    // 🔐 TOTP components (INSIDE CLASS ✅)
    private final DefaultSecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final DefaultCodeGenerator codeGenerator = new DefaultCodeGenerator();
    private final DefaultCodeVerifier verifier =
            new DefaultCodeVerifier(codeGenerator, new SystemTimeProvider());

    // ================= GENERATE SECRET + QR =================
    public Map<String, String> generateSetup(String email) {

        UserEntity user = getUser(email);

        String secret = secretGenerator.generate();

        String qrUrl = String.format(
                "otpauth://totp/Authify:%s?secret=%s&issuer=Authify",
                email,
                secret
        );

        user.setTwoFactorSecret(secret);
        userRepository.save(user);

        return Map.of(
                "secret", secret,
                "qrUrl", qrUrl
        );
    }

    // ================= VERIFY & ENABLE =================
    public void verifyAndEnable(String email, String code) {

        UserEntity user = getUser(email);

        if (user.getTwoFactorSecret() == null) {
            throw new RuntimeException("2FA setup not initiated");
        }

        boolean isValid = verifier.isValidCode(user.getTwoFactorSecret(), code);

        if (!isValid) {
            throw new RuntimeException("Invalid OTP");
        }

        user.setTwoFactorEnabled(true);
        userRepository.save(user);
    }

    // ================= VALIDATE DURING LOGIN =================
    public void validateLoginOtp(String email, String code) {

        UserEntity user = getUser(email);

        if (!Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            throw new RuntimeException("2FA not enabled");
        }

        boolean isValid = verifier.isValidCode(user.getTwoFactorSecret(), code);

        if (!isValid) {
            throw new RuntimeException("Invalid 2FA code");
        }
    }

    // ================= DISABLE =================
    public void disable(String email) {

        UserEntity user = getUser(email);

        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);

        userRepository.save(user);
    }

    // ================= HELPER =================
    private UserEntity getUser(String email) {

        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}