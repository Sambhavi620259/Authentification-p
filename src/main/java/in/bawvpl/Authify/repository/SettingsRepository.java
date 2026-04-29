package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.Settings;
import in.bawvpl.Authify.entity.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettingsRepository extends JpaRepository<Settings, Long> {

    Optional<Settings> findByUser(UserEntity user);
}