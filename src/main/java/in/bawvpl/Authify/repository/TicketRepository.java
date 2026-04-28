package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<TicketEntity, Long> {

    // ✅ get tickets by user
    List<TicketEntity> findByUser_IdOrderByCreatedAtDesc(Long userId);
}