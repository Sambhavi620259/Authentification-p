package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.FavoriteEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<FavoriteEntity, Long> {

    // ================= EXISTS =================
    boolean existsByUser_IdAndApp_AppId(Long userId, Long appId);

    // ================= GET =================
    List<FavoriteEntity> findByUser_Id(Long userId);

    // ================= FIND ONE =================
    Optional<FavoriteEntity> findByUser_IdAndApp_AppId(Long userId, Long appId);

    // ================= DELETE =================
    void deleteByUser_IdAndApp_AppId(Long userId, Long appId);
}