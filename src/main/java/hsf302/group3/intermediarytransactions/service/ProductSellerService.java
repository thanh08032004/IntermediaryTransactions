package hsf302.group3.intermediarytransactions.service;

import hsf302.group3.intermediarytransactions.entity.Product;
import hsf302.group3.intermediarytransactions.entity.ProductStatus;
import hsf302.group3.intermediarytransactions.entity.User;
import hsf302.group3.intermediarytransactions.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductSellerService {

    @Autowired
    private ProductRepository productRepository;

    // LIST của seller
    public Page<Product> getSellerProducts(Integer sellerId, String keyword, Pageable pageable) {
        return productRepository.searchBySeller(sellerId, keyword, pageable);
    }

    // DETAIL
    public Product getById(Integer id, Integer sellerId) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!p.getSupplier().getId().equals(sellerId)) {
            throw new RuntimeException("Access denied");
        }
        return p;
    }

    // CREATE
    public Product create(Product product, Integer sellerId) {
        product.setId(null);
        product.setSupplier(new User(sellerId)); // set seller
        product.setStatus(ProductStatus.ACTIVE);

        return productRepository.save(product);
    }

    // UPDATE
    public Product update(Integer id, Product updated, Integer sellerId) {
        Product old = getById(id, sellerId);

        old.setName(updated.getName());
        old.setPrice(updated.getPrice());
        old.setQuantity(updated.getQuantity());
        old.setDescription(updated.getDescription());
        old.setCategory(updated.getCategory());
        old.setStatus(updated.getStatus());

        return productRepository.save(old);
    }

    // DELETE (soft)
    public void delete(Integer id, Integer sellerId) {
        Product p = getById(id, sellerId);
        p.setStatus(ProductStatus.INACTIVE);
        productRepository.save(p);
    }
}