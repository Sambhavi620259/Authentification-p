package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.ApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ApplicationRepository extends JpaRepository<ApplicationEntity, Long> {

    @Query("SELECT COUNT(a) FROM ApplicationEntity a WHERE a.user.id = :userId")
    Integer countByUserId(Long userId);
}