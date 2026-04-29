package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.io.ApiResponse;
import in.bawvpl.Authify.io.TicketMessageResponse;
import in.bawvpl.Authify.io.TicketResponse;
import in.bawvpl.Authify.service.TicketService;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1.0/tickets")
@RequiredArgsConstructor
@CrossOrigin("*")
public class TicketController {

    private final TicketService ticketService;

    // ================= HELPER =================
    private String getEmail(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new RuntimeException("Unauthorized");
        }
        return auth.getName().toLowerCase().trim();
    }

    // ================= CREATE =================
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<TicketResponse>> create(
            Authentication auth,
            @RequestBody CreateReq req
    ) {

        TicketResponse res = ticketService.create(
                getEmail(auth),
                req.getSubject(),
                req.getMessage()
        );

        return ResponseEntity.ok(
                ApiResponse.<TicketResponse>builder()
                        .status(200)
                        .message("Ticket created")
                        .data(res)
                        .build()
        );
    }

    // ================= MY TICKETS =================
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> myTickets(
            Authentication auth
    ) {

        List<TicketResponse> list =
                ticketService.getUserTickets(getEmail(auth));

        return ResponseEntity.ok(
                ApiResponse.<List<TicketResponse>>builder()
                        .status(200)
                        .message("Tickets fetched")
                        .data(list)
                        .build()
        );
    }

    // ================= DETAIL =================
    @GetMapping("/{ticketId}")
    public ResponseEntity<ApiResponse<List<TicketMessageResponse>>> detail(
            Authentication auth,
            @PathVariable Long ticketId
    ) {

        List<TicketMessageResponse> messages =
                ticketService.getMessages(ticketId, getEmail(auth));

        return ResponseEntity.ok(
                ApiResponse.<List<TicketMessageResponse>>builder()
                        .status(200)
                        .message("Ticket messages fetched")
                        .data(messages)
                        .build()
        );
    }

    // ================= USER REPLY =================
    @PostMapping("/reply")
    public ResponseEntity<ApiResponse<String>> reply(
            Authentication auth,
            @RequestBody ReplyReq req
    ) {

        ticketService.replyUser(
                req.getTicketId(),
                req.getMessage(),
                getEmail(auth)
        );

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .status(200)
                        .message("Reply sent")
                        .data(null)
                        .build()
        );
    }

    // ================= DTOs =================
    @Data
    public static class CreateReq {
        private String subject;
        private String message;
    }

    @Data
    public static class ReplyReq {
        private Long ticketId;
        private String message;
    }
}