package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.SupportQuery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportQueryRepository extends JpaRepository<SupportQuery, Long> {

    // ✅ FIXED
    List<SupportQuery> findByUser_Id(Long userId);

    // ✅ KEEP THIS
    List<SupportQuery> findByQueryStatus(String status);
}