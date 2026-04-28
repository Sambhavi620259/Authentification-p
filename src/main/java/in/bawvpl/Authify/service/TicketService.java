package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.*;
import in.bawvpl.Authify.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketMessageRepository messageRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // ================= CREATE =================
    public TicketEntity create(String email, String subject, String message) {

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TicketEntity ticket = TicketEntity.builder()
                .user(user)
                .subject(subject)
                .status(TicketEntity.Status.OPEN)
                .createdAt(LocalDateTime.now())
                .build();

        ticket = ticketRepository.save(ticket);

        messageRepository.save(
                TicketMessageEntity.builder()
                        .ticket(ticket) // ✅ FIXED
                        .sender("USER")
                        .message(message)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        return ticket;
    }

    // ================= USER TICKETS =================
    public List<TicketEntity> getUserTickets(String email) {

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ticketRepository.findByUser_IdOrderByCreatedAtDesc(user.getId());
    }

    // ================= GET MESSAGES =================
    public List<TicketMessageEntity> getMessages(Long ticketId, String email) {

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (!ticket.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        return messageRepository.findByTicket_IdOrderByCreatedAtAsc(ticketId);
    }

    // ================= USER REPLY =================
    public void replyUser(Long ticketId, String message, String email) {

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (!ticket.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        if (ticket.getStatus() == TicketEntity.Status.CLOSED) {
            throw new RuntimeException("Ticket closed");
        }

        ticket.setStatus(TicketEntity.Status.IN_PROGRESS);
        ticketRepository.save(ticket);

        messageRepository.save(
                TicketMessageEntity.builder()
                        .ticket(ticket) // ✅ FIXED
                        .sender("USER")
                        .message(message)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    // ================= ADMIN REPLY =================
    public void replyAdmin(Long ticketId, String message) {

        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (ticket.getStatus() == TicketEntity.Status.CLOSED) {
            throw new RuntimeException("Ticket closed");
        }

        ticket.setStatus(TicketEntity.Status.IN_PROGRESS);
        ticketRepository.save(ticket);

        messageRepository.save(
                TicketMessageEntity.builder()
                        .ticket(ticket) // ✅ FIXED
                        .sender("ADMIN")
                        .message(message)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        // 🔔 Notification
        notificationRepository.save(
                NotificationEntity.builder()
                        .user(ticket.getUser())
                        .title("Ticket Update")
                        .message("Admin replied to your ticket")
                        .read(false)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    // ================= CLOSE =================
    public void close(Long ticketId, String email) {

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (!ticket.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        ticket.setStatus(TicketEntity.Status.CLOSED);
        ticketRepository.save(ticket);
    }
}