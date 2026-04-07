package in.bawvpl.Authify.io;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemResponse {
    private String id;
    private String userId;
    private String productId;
    private String productName;
    private double price;
    private int quantity;
}