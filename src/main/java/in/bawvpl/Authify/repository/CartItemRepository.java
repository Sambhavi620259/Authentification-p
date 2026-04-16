package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.CartItem;
import in.bawvpl.Authify.entity.Cart;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCart(Cart cart);

    Optional<CartItem> findByCartAndProductId(Cart cart, String productId);

    void deleteByCartAndProductId(Cart cart, String productId);
}