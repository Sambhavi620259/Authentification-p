package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.ApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppRepository extends JpaRepository<ApplicationEntity, Long> {

    // ================= ACTIVE APPS =================
    List<ApplicationEntity> findByStatus(String status);

    // ================= SEARCH =================
    List<ApplicationEntity> findByAppNameContainingIgnoreCase(String name);

    // ================= FILTER BY TYPE =================
    List<ApplicationEntity> findByAppType(String appType);

    // ================= COMBINED =================
    List<ApplicationEntity> findByStatusAndAppType(String status, String appType);
}