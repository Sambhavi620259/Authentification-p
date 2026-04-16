package in.bawvpl.Authify.service;

import in.bawvpl.Authify.io.CartItemRequest;
import in.bawvpl.Authify.io.CartItemResponse;

import java.util.List;

public interface CartItemService {

    CartItemResponse addItem(String email, CartItemRequest req);

    List<CartItemResponse> getItemsForUser(String email);

    void removeItem(String email, String productId);
}