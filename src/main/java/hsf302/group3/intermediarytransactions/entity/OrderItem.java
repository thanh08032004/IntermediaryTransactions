package hsf302.group3.intermediarytransactions.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItemDetail> orderItemDetails = new ArrayList<>();
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)

    private Product product;

    private BigDecimal price;

    private Integer quantity;

    private BigDecimal subtotal;

    public void calculateSubtotal() {
        this.subtotal = price.multiply(BigDecimal.valueOf(quantity));
    }
}
