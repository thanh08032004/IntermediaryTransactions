package hsf302.group3.intermediarytransactions.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products")
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

    @NotBlank
    @Column(nullable = false, length = 255)
    private String name;

    // Image Product
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductImage> images;

    // Category relationship
    @NotNull
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

    // Status Product
    @NotNull
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ProductStatus status;


    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    public String getMainImageUrl() {
        if (images != null) {
            for (ProductImage img : images) {
                if (Boolean.TRUE.equals(img.getIsMain())) {
                    return img.getImageUrl();
                }
            }
            if (!images.isEmpty()) {
                return images.get(0).getImageUrl();
            }
        }
        return "/images/default.jpg";
    }
}
