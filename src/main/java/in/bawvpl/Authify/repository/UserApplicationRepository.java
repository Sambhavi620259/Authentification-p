package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.UserApplicationEntity;
import in.bawvpl.Authify.entity.UserEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserApplicationRepository extends JpaRepository<UserApplicationEntity, Long> {

    // ================= FIND ONE =================
    Optional<UserApplicationEntity> findByUser_IdAndApp_AppId(Long userId, Long appId);

    // ================= EXISTS =================
    boolean existsByUser_IdAndApp_AppId(Long userId, Long appId);

    // ================= LIST =================
    List<UserApplicationEntity> findAllByUser(UserEntity user);

    List<UserApplicationEntity> findAllByUser_Id(Long userId);

    // ================= PAGINATION (🔥 REQUIRED FIX) =================
    Page<UserApplicationEntity> findAllByUser_Id(Long userId, Pageable pageable);

}