package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.TicketMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketMessageRepository extends JpaRepository<TicketMessageEntity, Long> {

    // ✅ use relation
    List<TicketMessageEntity> findByTicket_IdOrderByCreatedAtAsc(Long ticketId);
}