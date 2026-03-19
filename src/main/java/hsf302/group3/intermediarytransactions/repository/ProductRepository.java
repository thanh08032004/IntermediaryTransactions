package hsf302.group3.intermediarytransactions.repository;

import hsf302.group3.intermediarytransactions.entity.Product;
import hsf302.group3.intermediarytransactions.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    boolean existsByCategoryId(Integer categoryId);
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    Page<Product> findByStatusAndPriceBetween(
            ProductStatus status, Double min, Double max, Pageable pageable);

    Page<Product> findByStatusAndPriceGreaterThanEqual(
            ProductStatus status, Double min, Pageable pageable);

    Page<Product> findByStatusAndPriceLessThanEqual(
            ProductStatus status, Double max, Pageable pageable);


    // lấy product theo seller
    Page<Product> findBySupplierId(Integer supplierId, Pageable pageable);

    // search theo seller
    @Query("""
        SELECT p FROM Product p
        WHERE p.supplier.id = :supplierId
        AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
    """)
    Page<Product> searchBySeller(
            @Param("supplierId") Integer supplierId,
            @Param("keyword") String keyword,
            Pageable pageable
    );


}
