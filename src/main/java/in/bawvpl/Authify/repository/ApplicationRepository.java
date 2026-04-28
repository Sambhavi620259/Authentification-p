package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.ApplicationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<ApplicationEntity, Long> {

    // ================= USER =================
    long countByUser_Id(Long userId);

    Page<ApplicationEntity> findByUser_Id(Long userId, Pageable pageable);

    // ================= STATUS =================
    Page<ApplicationEntity> findByStatus(String status, Pageable pageable);

    // ✅ Optional (useful for non-paginated cases)
    boolean existsByAppIdAndStatus(Long appId, String status);

    // ================= SEARCH =================
    Page<ApplicationEntity> findByAppNameContainingIgnoreCase(
            String appName,
            Pageable pageable
    );

    // ================= FILTER + SEARCH =================
    Page<ApplicationEntity> findByStatusAndAppNameContainingIgnoreCase(
            String status,
            String appName,
            Pageable pageable
    );
}