package in.bawvpl.Authify.service;

import in.bawvpl.Authify.io.CartItemRequest;
import in.bawvpl.Authify.io.CartItemResponse;

import java.util.List;

public interface CartItemService {

    CartItemResponse addItem(String userId, CartItemRequest req);

    List<CartItemResponse> getItemsForUser(String userId);

    void removeItem(String userId, String productId);
}
