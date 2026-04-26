package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.entity.TicketEntity;
import in.bawvpl.Authify.entity.TicketMessageEntity;
import in.bawvpl.Authify.io.ApiResponse;
import in.bawvpl.Authify.service.TicketService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1.0/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    // ✅ CREATE
    @PostMapping
    public ResponseEntity<ApiResponse<TicketEntity>> create(@RequestBody CreateReq req) {

        return ResponseEntity.ok(
                ApiResponse.<TicketEntity>builder()
                        .status(200)
                        .message("Ticket created")
                        .data(ticketService.create(req.userId, req.subject, req.message))
                        .build()
        );
    }

    // ✅ LIST
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<TicketEntity>>> list(@PathVariable Long userId) {

        return ResponseEntity.ok(
                ApiResponse.<List<TicketEntity>>builder()
                        .status(200)
                        .message("Tickets fetched")
                        .data(ticketService.getUserTickets(userId))
                        .build()
        );
    }

    // ✅ DETAIL
    @GetMapping("/detail/{ticketId}")
    public ResponseEntity<ApiResponse<List<TicketMessageEntity>>> detail(@PathVariable Long ticketId) {

        return ResponseEntity.ok(
                ApiResponse.<List<TicketMessageEntity>>builder()
                        .status(200)
                        .message("Messages fetched")
                        .data(ticketService.getMessages(ticketId))
                        .build()
        );
    }

    // ✅ REPLY
    @PostMapping("/{ticketId}/reply")
    public ResponseEntity<ApiResponse<Object>> reply(
            @PathVariable Long ticketId,
            @RequestBody ReplyReq req
    ) {

        ticketService.reply(ticketId, req.message, req.sender);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("Reply sent")
                        .data(null)
                        .build()
        );
    }

    // ✅ CLOSE
    @PutMapping("/{ticketId}/close")
    public ResponseEntity<ApiResponse<Object>> close(@PathVariable Long ticketId) {

        ticketService.close(ticketId);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status(200)
                        .message("Ticket closed")
                        .data(null)
                        .build()
        );
    }

    // DTOs
    @Data
    static class CreateReq {
        public Long userId;
        public String subject;
        public String message;
    }

    @Data
    static class ReplyReq {
        public String message;
        public String sender; // USER / ADMIN
    }
}
