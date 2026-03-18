package hsf302.group3.intermediarytransactions.service;

import hsf302.group3.intermediarytransactions.entity.Product;
import hsf302.group3.intermediarytransactions.repository.ProductUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductBuyerService {
    @Autowired
    private ProductUserRepository productRepository;

    public Page<Product> getAllActiveProducts(Pageable pageable) {
        return productRepository.findByStatus("ACTIVE", pageable);
    }

    public Product getProductById(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public Page<Product> searchProducts(String keyword, Integer categoryId, Pageable pageable) {
        return productRepository.searchProduct(keyword, categoryId, pageable);
    }
}
