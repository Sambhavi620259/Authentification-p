package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<WalletEntity, Long> {

    // ✅ Fetch wallet safely by user ID
    Optional<WalletEntity> findByUser_Id(Long userId);
}