package in.bawvpl.Authify.service;

import in.bawvpl.Authify.entity.CartItem;
import in.bawvpl.Authify.io.CartItemRequest;
import in.bawvpl.Authify.io.CartItemResponse;
import in.bawvpl.Authify.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;

    // ADD ITEM
    @Override
    @Transactional
    public CartItemResponse addItem(String userId, CartItemRequest req) {

        CartItem existing = cartItemRepository.findByUserIdAndProductId(userId, req.getProductId())
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + req.getQuantity());
            existing.setPrice(req.getPrice());
            return toResponse(cartItemRepository.save(existing));
        }

        CartItem item = CartItem.builder()
                .userId(userId)
                .productId(req.getProductId())
                .productName(req.getProductName())
                .price(req.getPrice())
                .quantity(req.getQuantity())
                .build();

        return toResponse(cartItemRepository.save(item));
    }

    // GET ITEMS FOR USER
    @Override
    public List<CartItemResponse> getItemsForUser(String userId) {
        return cartItemRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // REMOVE ITEM
    @Override
    @Transactional
    public void removeItem(String userId, String productId) {

        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Item not found"));

        cartItemRepository.delete(item);
    }

    private CartItemResponse toResponse(CartItem item) {
        return CartItemResponse.builder()
                .id(item.getId())
                .userId(item.getUserId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .build();
    }
}
