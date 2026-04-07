package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.SupportQuery;
import in.bawvpl.Authify.service.SupportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1.0/support")
@RequiredArgsConstructor
public class SupportController {

    private final SupportService supportService;

    // ✅ CREATE QUERY
    @PostMapping
    public SupportQuery create(
            @RequestParam Long userId,
            @RequestParam Long appId,
            @RequestParam String query
    ) {
        return supportService.createQuery(userId, appId, query);
    }

    // ✅ ADMIN REPLY
    @PostMapping("/reply")
    public SupportQuery reply(
            @RequestParam Long queryId,
            @RequestParam Long adminId,
            @RequestParam String answer
    ) {
        return supportService.replyQuery(queryId, adminId, answer);
    }

    // ✅ GET USER QUERIES
    @GetMapping("/user/{userId}")
    public List<SupportQuery> userQueries(@PathVariable Long userId) {
        return supportService.getUserQueries(userId);
    }
}