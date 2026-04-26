package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.KycEntity;
import in.bawvpl.Authify.entity.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface KycRepository extends JpaRepository<KycEntity, Long> {

    // ✅ REQUIRED
    Optional<KycEntity> findByUser(UserEntity user);

    // ✅ ALSO REQUIRED
    Optional<KycEntity> findByUser_Id(Long userId);

    boolean existsByUser_Id(Long userId);

    List<KycEntity> findByStatusIgnoreCase(String status);

    long countByStatusIgnoreCase(String status);

    List<KycEntity> findAllByOrderByUploadedAtDesc();
}