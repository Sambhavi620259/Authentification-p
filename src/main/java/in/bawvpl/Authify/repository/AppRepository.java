package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.AppEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppRepository extends JpaRepository<AppEntity, Long> {

    // ================= ACTIVE APPS =================
    List<AppEntity> findByStatus(String status);

    // ================= SEARCH =================
    List<AppEntity> findByAppNameContainingIgnoreCase(String name);

    // ================= FILTER BY TYPE =================
    List<AppEntity> findByAppType(String appType);

    // ================= COMBINED =================
    List<AppEntity> findByStatusAndAppType(String status, String appType);
}