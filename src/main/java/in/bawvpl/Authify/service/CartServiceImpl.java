package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.Cart;
import in.bawvpl.Authify.entity.CartItem;
import in.bawvpl.Authify.entity.UserEntity;
import in.bawvpl.Authify.io.CartItemRequest;
import in.bawvpl.Authify.io.CartItemResponse;
import in.bawvpl.Authify.repository.CartItemRepository;
import in.bawvpl.Authify.repository.CartRepository;
import in.bawvpl.Authify.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    // ================= ADD ITEM =================
    @Override
    @Transactional
    public CartItemResponse addItem(String email, CartItemRequest req) {

        email = email.toLowerCase().trim();

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));

        if (req.getProductId() == null || req.getProductId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product ID required");
        }

        if (req.getQuantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid quantity");
        }

        if (req.getPrice() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid price");
        }

        CartItem existing = cartItemRepository
                .findByCartAndProductId(cart, req.getProductId())
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + req.getQuantity());
            existing.setPrice(req.getPrice());
            return toResponse(cartItemRepository.save(existing));
        }

        CartItem item = CartItem.builder()
                .cart(cart)
                .productId(req.getProductId())
                .productName(req.getProductName())
                .price(req.getPrice())
                .quantity(req.getQuantity())
                .build();

        return toResponse(cartItemRepository.save(item));
    }

    // ================= GET ITEMS =================
    @Override
    public Page<CartItemResponse> getItemsForUser(String email, int page, int size) {

        email = email.toLowerCase().trim();

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<CartItem> cartItems = cartItemRepository.findByCart(cart, pageable);

        return cartItems.map(this::toResponse);
    }

    // ================= REMOVE ITEM =================
    @Override
    @Transactional
    public void removeItem(String email, String productId) {

        email = email.toLowerCase().trim();

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));

        CartItem item = cartItemRepository
                .findByCartAndProductId(cart, productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        cartItemRepository.delete(item);
    }

    // ================= MAPPER =================
    private CartItemResponse toResponse(CartItem item) {
        return CartItemResponse.builder()
                .id(item.getId())
                .userId(item.getCart().getUser().getEmail())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .build();
    }
}