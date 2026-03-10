package hsf302.group3.intermediarytransactions.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "product_code", unique = true, length = 50)
    private String productCode;

    @Column(nullable = false, length = 255)
    private String name;

    // Category relationship
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    // Supplier relationship (User)
    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private User supplier;

    @Column(precision = 15, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    private Integer quantity = 0;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
