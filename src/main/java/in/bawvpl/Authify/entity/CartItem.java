package in.bawvpl.Authify.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String productId;
    private String productName;
    private double price;
    private int quantity;

    private String userId;

    // ✅ IMPORTANT FIX (must match Cart)
    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;
}