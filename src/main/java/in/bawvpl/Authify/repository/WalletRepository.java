package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.WalletEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface WalletRepository extends JpaRepository<WalletEntity, Long> {

    @Query("SELECT COALESCE(w.balance, 0) FROM WalletEntity w WHERE w.user.id = :userId")
    Double findBalanceByUserId(@Param("userId") Long userId);
}