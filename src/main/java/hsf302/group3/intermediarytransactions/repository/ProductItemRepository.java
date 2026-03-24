package hsf302.group3.intermediarytransactions.repository;

import hsf302.group3.intermediarytransactions.entity.ItemStatus;
import hsf302.group3.intermediarytransactions.entity.Product;
import hsf302.group3.intermediarytransactions.entity.ProductItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductItemRepository extends JpaRepository<ProductItem, Integer> {

    // Lấy tất cả item theo product
    List<ProductItem> findByProductId(Integer productId);

    // Lấy item AVAILABLE
    List<ProductItem> findByProductIdAndStatus(Integer productId, ItemStatus status);
    List<ProductItem> findByProductAndStatus(Product product, ItemStatus status);
    Optional<ProductItem> findFirstByProductAndBuyerIsNull(Product product);
}