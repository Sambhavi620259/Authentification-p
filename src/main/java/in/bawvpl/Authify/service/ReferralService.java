package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReferralService {

    private final UserRepository userRepository;

    // ================= GENERATE UNIQUE REFERRAL CODE =================
    public String generateUniqueReferralCode() {

        String code;

        do {
            code = "REF" + UUID.randomUUID().toString()
                    .replace("-", "")
                    .substring(0, 8)
                    .toUpperCase();

        } while (userRepository.findByReferralCode(code).isPresent());

        return code;
    }

    // ================= APPLY REFERRAL =================
    public void applyReferral(UserEntity newUser, String referralCode) {

        if (referralCode == null || referralCode.isBlank()) {
            return;
        }

        Optional<UserEntity> referrer =
                userRepository.findByReferralCode(referralCode.trim());

        if (referrer.isPresent()) {

            // link new user to referrer
            newUser.setReferredBy(referralCode.trim());

            // OPTIONAL: you can add reward logic here later
            // e.g., referrer.get().setPoints(...)
        }
    }
}