package in.bawvpl.Authify.io;

import lombok.Data;

@Data
public class CartItemRequest {
    private String productId;
    private String productName;
    private double price;
    private int quantity;
}