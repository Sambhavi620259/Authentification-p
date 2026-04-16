package in.bawvpl.Authify.io;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {

    private Long id; // 🔥 FIXED (was String ❌)

    private String userId; // email
    private String productId;
    private String productName;
    private double price;
    private int quantity;
}