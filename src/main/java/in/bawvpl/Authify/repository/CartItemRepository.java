package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, String> {

    Optional<CartItem> findByUserIdAndProductId(String userId, String productId);

    List<CartItem> findByUserId(String userId);
}