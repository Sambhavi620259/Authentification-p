package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.ApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<ApplicationEntity, Long> {

    long countByUser_Id(Long userId);

    List<ApplicationEntity> findByUser_Id(Long userId);

    // 🔥 FIXED SEARCH METHOD
    List<ApplicationEntity> findByAppNameContainingIgnoreCase(String appName);
}