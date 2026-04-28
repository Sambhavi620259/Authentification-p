package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.FavoriteEntity;
import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.entity.ApplicationEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<FavoriteEntity, Long> {

    List<FavoriteEntity> findByUserOrderByCreatedAtDesc(UserEntity user);

    Optional<FavoriteEntity> findByUserAndApp(UserEntity user, ApplicationEntity app);

    void deleteByUserAndApp(UserEntity user, ApplicationEntity app);

    boolean existsByUserAndApp(UserEntity user, ApplicationEntity app);
}