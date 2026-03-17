package hsf302.group3.intermediarytransactions.service;

import hsf302.group3.intermediarytransactions.entity.Category;
import hsf302.group3.intermediarytransactions.entity.Product;
import hsf302.group3.intermediarytransactions.entity.ProductImage;
import hsf302.group3.intermediarytransactions.repository.CategoryRepository;
import hsf302.group3.intermediarytransactions.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    //list product
    public Page<Product> getProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return productRepository.findAll(pageable);
    }

    //Search product
    public Page<Product> searchProducts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if (keyword == null || keyword.isEmpty()) {
            return productRepository.findAll(pageable);
        }
        return productRepository.findByNameContainingIgnoreCase(keyword, pageable);
    }

    //Lay product bang id
    public Product getProductById(int id){
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    //save product
    public void saveProduct(Product product){

        if(product.getStatus() == null){
            product.setStatus(Product.Status.ACTIVE);
        }

        if(product.getCategory() != null){
            Category category = categoryRepository
                    .findById(product.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        productRepository.save(product);
    }

    //xoa product
    public void deleteProduct(int id){
        productRepository.deleteById(id);
    }

    //list Category
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    //Đổi trạng thái status
    public void toggleStatus(int id){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if(product.getStatus() == Product.Status.ACTIVE){
            product.setStatus(Product.Status.INACTIVE);
        } else {
            product.setStatus(Product.Status.ACTIVE);
        }

        productRepository.save(product);
    }
}