package hsf302.group3.intermediarytransactions.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @NotNull
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private User supplier;

    @Column(precision = 15, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductItem> productItems;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    @Transient
    private MultipartFile[] uploadedFiles;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    @Transient
    public int getAvailableQuantity() {
        if (productItems == null) return 0;
        return (int) productItems.stream()
                .filter(item -> item.getStatus() != null
                        && item.getStatus() == ItemStatus.AVAILABLE)
                .count();
    }

    @Transient
    public String getMainImageUrl() {
        if (images != null && !images.isEmpty()) {
            for (ProductImage img : images) {
                if (Boolean.TRUE.equals(img.getIsMain())) return img.getImageUrl();
            }
            return images.get(0).getImageUrl();
        }
        return "/images/default.jpg";
    }
}