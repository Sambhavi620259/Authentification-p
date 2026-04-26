package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.TicketEntity;
import in.bawvpl.Authify.entity.TicketMessageEntity;
import in.bawvpl.Authify.repository.TicketMessageRepository;
import in.bawvpl.Authify.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketMessageRepository messageRepository;

    // ✅ CREATE TICKET
    public TicketEntity create(Long userId, String subject, String message) {

        TicketEntity ticket = TicketEntity.builder()
                .userId(userId)
                .subject(subject)
                .status("OPEN")
                .createdAt(LocalDateTime.now())
                .build();

        ticket = ticketRepository.save(ticket);

        messageRepository.save(
                TicketMessageEntity.builder()
                        .ticketId(ticket.getId())
                        .sender("USER")
                        .message(message)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        return ticket;
    }

    // ✅ LIST
    public List<TicketEntity> getUserTickets(Long userId) {
        return ticketRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // ✅ DETAIL
    public List<TicketMessageEntity> getMessages(Long ticketId) {
        return messageRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
    }

    // ✅ REPLY
    public void reply(Long ticketId, String message, String sender) {

        messageRepository.save(
                TicketMessageEntity.builder()
                        .ticketId(ticketId)
                        .sender(sender)
                        .message(message)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    // ✅ CLOSE
    public void close(Long ticketId) {
        TicketEntity t = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        t.setStatus("CLOSED");
        ticketRepository.save(t);
    }
}
