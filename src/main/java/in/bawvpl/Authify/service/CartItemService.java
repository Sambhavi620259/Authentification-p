package in.bawvpl.Authify.service;

import in.bawvpl.Authify.io.CartItemRequest;
import in.bawvpl.Authify.io.CartItemResponse;
import org.springframework.data.domain.Page;

public interface CartItemService {

    CartItemResponse addItem(String email, CartItemRequest req);

    Page<CartItemResponse> getItemsForUser(String email, int page, int size);

    void removeItem(String email, String productId);
}