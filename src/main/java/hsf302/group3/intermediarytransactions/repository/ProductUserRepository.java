package hsf302.group3.intermediarytransactions.repository;

import hsf302.group3.intermediarytransactions.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductUserRepository extends JpaRepository<Product, Integer> {

    // 1. Lấy list product ACTIVE
    Page<Product> findByStatus(String status, Pageable pageable);

    // 2. Search theo name + status
    Page<Product> findByNameContainingIgnoreCaseAndStatus(
            String keyword, String status, Pageable pageable);

    // 3. Filter theo category + status
    Page<Product> findByCategory_IdAndStatus(
            Integer categoryId, String status, Pageable pageable);

    // 4. Filter theo price range + status
    Page<Product> findByPriceBetweenAndStatus(
            Double min, Double max, String status, Pageable pageable);

    // 5. Kết hợp nhiều điều kiện (advanced)
    @Query("""
        SELECT p FROM Product p
        WHERE p.status = 'ACTIVE'
        AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:categoryId IS NULL OR p.category.id = :categoryId)
    
    """)
    Page<Product> searchProduct(
            @Param("keyword") String keyword,
            @Param("categoryId") Integer categoryId,
            Pageable pageable
    );
}
