package hsf302.group3.intermediarytransactions.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_item_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    @OneToOne
    @JoinColumn(name = "product_item_id")
    private ProductItem productItem;
}