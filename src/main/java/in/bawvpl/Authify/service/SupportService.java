package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.*;
import in.bawvpl.Authify.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupportService {

    private final SupportQueryRepository supportRepository;
    private final UserRepository userRepository;
    private final AppRepository appRepository;

    // ================= CREATE QUERY =================
    public SupportQuery createQuery(Long userId, Long appId, String query) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

       ApplicationEntity app = appRepository.findById(appId)
                .orElseThrow(() -> new RuntimeException("App not found"));

        SupportQuery support = SupportQuery.builder()
                .user(user)
                .app(app)
                .queryAnswer(query)
                .queryStatus("Open")
                .build();

        return supportRepository.save(support);
    }

    // ================= ADMIN REPLY =================
    public SupportQuery replyQuery(Long queryId, Long adminId, String answer) {

        SupportQuery query = supportRepository.findById(queryId)
                .orElseThrow(() -> new RuntimeException("Query not found"));

        String updatedText = query.getQueryAnswer() + "\n\nADMIN: " + answer;

        query.setQueryAnswer(updatedText);
        query.setAttendedBy(adminId);
        query.setQueryStatus("Closed");

        return supportRepository.save(query);
    }

    // ================= GET USER QUERIES =================
    public List<SupportQuery> getUserQueries(Long userId) {

        // ✅ FINAL FIX HERE
        return supportRepository.findByUser_Id(userId);
    }
}