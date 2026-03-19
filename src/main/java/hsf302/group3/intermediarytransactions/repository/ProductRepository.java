package hsf302.group3.intermediarytransactions.repository;

import hsf302.group3.intermediarytransactions.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
    boolean existsByCategoryId(Integer categoryId);
    Page<Product> findByStatus(Product.Status status, Pageable pageable);

    Page<Product> findByStatusAndPriceBetween(
            Product.Status status, Double min, Double max, Pageable pageable);

    Page<Product> findByStatusAndPriceGreaterThanEqual(
            Product.Status status, Double min, Pageable pageable);

    Page<Product> findByStatusAndPriceLessThanEqual(
            Product.Status status, Double max, Pageable pageable);
    }
