package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.io.CartItemRequest;
import in.bawvpl.Authify.io.CartItemResponse;
import in.bawvpl.Authify.service.CartItemService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@CrossOrigin("*")
@Slf4j
public class CartItemController {

    private final CartItemService cartService;

    // ================= ADD ITEM =================
    @PostMapping("/items")
    public ResponseEntity<?> addItem(
            Authentication authentication,
            @Valid @RequestBody CartItemRequest req
    ) {
        try {
            String email = authentication.getName().toLowerCase().trim();

            CartItemResponse response = cartService.addItem(email, req);

            return ResponseEntity.ok(Map.of(
                    "message", "Item added to cart",
                    "data", response
            ));

        } catch (Exception e) {
            log.error("Add cart item error: {}", e.getMessage());

            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage()
            ));
        }
    }

    // ================= GET ITEMS =================
    @GetMapping("/items")
    public ResponseEntity<?> getItems(Authentication authentication) {

        try {
            String email = authentication.getName().toLowerCase().trim();

            List<CartItemResponse> items = cartService.getItemsForUser(email);

            return ResponseEntity.ok(Map.of(
                    "message", "Cart items fetched",
                    "data", items
            ));

        } catch (Exception e) {
            log.error("Get cart error: {}", e.getMessage());

            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage()
            ));
        }
    }

    // ================= REMOVE ITEM =================
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<?> removeItem(
            @PathVariable String productId,
            Authentication authentication
    ) {

        try {
            String email = authentication.getName().toLowerCase().trim();

            cartService.removeItem(email, productId);

            return ResponseEntity.ok(Map.of(
                    "message", "Item removed from cart"
            ));

        } catch (Exception e) {
            log.error("Remove cart item error: {}", e.getMessage());

            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage()
            ));
        }
    }
}