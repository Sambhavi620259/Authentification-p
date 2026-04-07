package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.AppEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppRepository extends JpaRepository<AppEntity, Long> {
}