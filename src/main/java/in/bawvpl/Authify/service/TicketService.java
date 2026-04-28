package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.*;
import in.bawvpl.Authify.io.TicketMessageResponse;
import in.bawvpl.Authify.io.TicketResponse;
import in.bawvpl.Authify.repository.*;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional // 🔥 VERY IMPORTANT
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketMessageRepository messageRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // ================= CREATE =================
    public TicketResponse create(String email, String subject, String message) {

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        TicketEntity ticket = ticketRepository.save(
                TicketEntity.builder()
                        .user(user)
                        .subject(subject)
                        .status(TicketEntity.Status.OPEN)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        messageRepository.save(
                TicketMessageEntity.builder()
                        .ticket(ticket)
                        .sender("USER")
                        .message(message)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        return toResponse(ticket);
    }

    // ================= USER TICKETS =================
    public List<TicketResponse> getUserTickets(String email) {

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return ticketRepository.findByUser_IdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ================= GET MESSAGES =================
    public List<TicketMessageResponse> getMessages(Long ticketId, String email) {

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        if (!ticket.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized");
        }

        return messageRepository.findByTicket_IdOrderByCreatedAtAsc(ticketId)
                .stream()
                .map(m -> TicketMessageResponse.builder()
                        .sender(m.getSender())
                        .message(m.getMessage())
                        .createdAt(m.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // ================= USER REPLY =================
    public void replyUser(Long ticketId, String message, String email) {

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        if (!ticket.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized");
        }

        if (ticket.getStatus() == TicketEntity.Status.CLOSED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket closed");
        }

        ticket.setStatus(TicketEntity.Status.IN_PROGRESS);

        messageRepository.save(
                TicketMessageEntity.builder()
                        .ticket(ticket)
                        .sender("USER")
                        .message(message)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    // ================= ADMIN REPLY =================
    public void replyAdmin(Long ticketId, String message) {

        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        if (ticket.getStatus() == TicketEntity.Status.CLOSED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket closed");
        }

        ticket.setStatus(TicketEntity.Status.IN_PROGRESS);

        messageRepository.save(
                TicketMessageEntity.builder()
                        .ticket(ticket)
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
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        if (!ticket.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized");
        }

        ticket.setStatus(TicketEntity.Status.CLOSED);
    }

    // ================= MAPPER =================
    private TicketResponse toResponse(TicketEntity t) {
        return TicketResponse.builder()
                .id(t.getId())
                .subject(t.getSubject())
                .status(t.getStatus().name())
                .createdAt(t.getCreatedAt())
                .build();
    }
}