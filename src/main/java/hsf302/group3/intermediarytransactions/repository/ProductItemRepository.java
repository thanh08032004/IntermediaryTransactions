package hsf302.group3.intermediarytransactions.repository;

import hsf302.group3.intermediarytransactions.entity.ItemStatus;
import hsf302.group3.intermediarytransactions.entity.ProductItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductItemRepository extends JpaRepository<ProductItem, Integer> {

    // Lấy tất cả item theo product
    List<ProductItem> findByProductId(Integer productId);

    // Lấy item AVAILABLE
    List<ProductItem> findByProductIdAndStatus(Integer productId, ItemStatus status);

}