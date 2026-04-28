package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.service.TicketService;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1.0/tickets")
@RequiredArgsConstructor
@CrossOrigin("*")
public class TicketController {

    private final TicketService ticketService;

    // ================= CREATE =================
    @PostMapping
    public ResponseEntity<?> create(
            Authentication auth,
            @RequestBody CreateReq req
    ) {
        return ResponseEntity.ok(
                ticketService.create(auth.getName(), req.getSubject(), req.getMessage())
        );
    }

    // ================= MY TICKETS =================
    @GetMapping("/my")
    public ResponseEntity<?> myTickets(Authentication auth) {
        return ResponseEntity.ok(
                ticketService.getUserTickets(auth.getName())
        );
    }

    // ================= DETAIL =================
    @GetMapping("/{ticketId}")
    public ResponseEntity<?> detail(
            Authentication auth,
            @PathVariable Long ticketId
    ) {
        return ResponseEntity.ok(
                ticketService.getMessages(ticketId, auth.getName())
        );
    }

    // ================= USER REPLY =================
    @PostMapping("/{ticketId}/reply")
    public ResponseEntity<?> reply(
            Authentication auth,
            @PathVariable Long ticketId,
            @RequestBody ReplyReq req
    ) {
        ticketService.replyUser(ticketId, req.getMessage(), auth.getName());
        return ResponseEntity.ok("Reply sent");
    }

    // ================= CLOSE =================
    @PutMapping("/{ticketId}/close")
    public ResponseEntity<?> close(
            Authentication auth,
            @PathVariable Long ticketId
    ) {
        ticketService.close(ticketId, auth.getName());
        return ResponseEntity.ok("Closed");
    }

    // ================= ADMIN REPLY =================
    @PostMapping("/admin/{ticketId}/reply")
    public ResponseEntity<?> adminReply(
            @PathVariable Long ticketId,
            @RequestBody ReplyReq req
    ) {
        ticketService.replyAdmin(ticketId, req.getMessage());
        return ResponseEntity.ok("Admin replied");
    }

    // ================= DTOs =================

    @Data
    public static class CreateReq {
        private String subject;
        private String message;
    }

    @Data
    public static class ReplyReq {
        private String message;
    }
}