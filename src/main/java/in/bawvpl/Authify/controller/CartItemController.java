package in.bawvpl.Authify.controller;

import in.bawvpl.Authify.io.CartItemRequest;
import in.bawvpl.Authify.io.CartItemResponse;
import in.bawvpl.Authify.service.CartItemService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1.0/cart") // 🔥 fixed version
@RequiredArgsConstructor
@CrossOrigin("*")
@Slf4j
public class CartItemController {

    private final CartItemService cartService;

    // ================= ADD ITEM =================
    @PostMapping("/items")
    public ResponseEntity<?> addItem(
            Authentication auth,
            @Valid @RequestBody CartItemRequest req
    ) {
        String email = auth.getName().toLowerCase().trim();

        return ResponseEntity.ok(Map.of(
                "message", "Item added",
                "data", cartService.addItem(email, req)
        ));
    }

    // ================= GET ITEMS (PAGINATION) =================
    @GetMapping("/items")
    public ResponseEntity<?> getItems(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String email = auth.getName().toLowerCase().trim();

        Page<CartItemResponse> items = cartService.getItemsForUser(email, page, size);

        return ResponseEntity.ok(Map.of(
                "message", "Cart items fetched",
                "data", items.getContent(),
                "totalPages", items.getTotalPages(),
                "totalElements", items.getTotalElements()
        ));
    }

    // ================= REMOVE ITEM =================
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<?> removeItem(
            @PathVariable String productId,
            Authentication auth
    ) {
        String email = auth.getName().toLowerCase().trim();

        cartService.removeItem(email, productId);

        return ResponseEntity.ok(Map.of(
                "message", "Item removed"
        ));
    }
}