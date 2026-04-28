package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.Cart;
import in.bawvpl.Authify.entity.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    // ✅ REQUIRED METHOD
    Optional<Cart> findByUser(UserEntity user);
}