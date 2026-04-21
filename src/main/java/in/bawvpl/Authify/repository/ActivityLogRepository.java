package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.ActivityLog;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    // ================= DASHBOARD =================
    Page<ActivityLog> findByUser_Id(Long userId, Pageable pageable);

    // ================= SORTED (BEST PRACTICE) =================
    Page<ActivityLog> findByUser_IdOrderByTimestampDesc(Long userId, Pageable pageable);
}