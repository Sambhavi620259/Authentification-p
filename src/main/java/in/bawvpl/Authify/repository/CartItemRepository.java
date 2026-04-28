package in.bawvpl.Authify.repository;

import in.bawvpl.Authify.entity.Cart;
import in.bawvpl.Authify.entity.CartItem;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // ✅ find specific item
    Optional<CartItem> findByCartAndProductId(Cart cart, String productId);

    // ✅ FIXED PAGINATION METHOD (THIS SOLVES YOUR ERROR)
    Page<CartItem> findByCart(Cart cart, Pageable pageable);
}