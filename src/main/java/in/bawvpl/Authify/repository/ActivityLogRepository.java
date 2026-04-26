package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.ActivityLog;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    // ✅ Sorted activities (USED in dashboard)
    Page<ActivityLog> findByUser_IdOrderByTimestampDesc(Long userId, Pageable pageable);
}