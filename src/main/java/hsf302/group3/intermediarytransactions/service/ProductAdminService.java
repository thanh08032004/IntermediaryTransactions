package hsf302.group3.intermediarytransactions.service;

import hsf302.group3.intermediarytransactions.entity.Category;
import hsf302.group3.intermediarytransactions.entity.Product;
import hsf302.group3.intermediarytransactions.entity.ProductImage;
import hsf302.group3.intermediarytransactions.repository.CategoryRepository;
import hsf302.group3.intermediarytransactions.repository.ProductImageRepository;
import hsf302.group3.intermediarytransactions.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ProductAdminService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;

    public ProductAdminService(ProductRepository productRepository,
                               CategoryRepository categoryRepository,
                               ProductImageRepository productImageRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productImageRepository = productImageRepository;
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

//    //save product
//    public void saveProduct(Product product){
//        if(product.getStatus() == null){
//            product.setStatus(Product.Status.ACTIVE);
//        }
//        if(product.getCategory() != null){
//            Category category = categoryRepository
//                    .findById(product.getCategory().getId())
//                    .orElseThrow(() -> new RuntimeException("Category not found"));
//            product.setCategory(category);
//        }
//        productRepository.save(product);
//    }

    public void saveProductWithImages(Product product,
                                      List<MultipartFile> files,
                                      Integer mainIndex) {

        if(product.getStatus() == null){
            product.setStatus(Product.Status.ACTIVE);
        }

        if(product.getCategory() != null){
            Category category = categoryRepository.findById(
                    product.getCategory().getId()
            ).orElseThrow(() -> new RuntimeException("Category not found"));

            product.setCategory(category);
        }

        productRepository.save(product);

        // ======================
        // upload ảnh
        // ======================
        if (files != null && !files.isEmpty()) {

            int index = 0;

            for (MultipartFile file : files) {
                if (!file.isEmpty()) {

                    String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

                    try {
                        java.nio.file.Path path = java.nio.file.Paths.get("uploads/" + fileName);
                        java.nio.file.Files.createDirectories(path.getParent());
                        java.nio.file.Files.write(path, file.getBytes());
                    } catch (Exception e) {
                        throw new RuntimeException("Upload fail");
                    }

                    ProductImage img = new ProductImage();
                    img.setImageUrl("/uploads/" + fileName);
                    img.setProduct(product);

                    if (mainIndex != null && index == mainIndex) {
                        img.setIsMain(true);
                    }

                    productImageRepository.save(img);
                    index++;
                }
            }
        }
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

    public void updateProductByAdmin(Product updatedProduct,
                                     List<MultipartFile> files,
                                     Integer mainIndex) {

        Product existing = productRepository.findById(updatedProduct.getId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // update basic
        existing.setName(updatedProduct.getName());
        existing.setDescription(updatedProduct.getDescription());

        // category
        if(updatedProduct.getCategory() != null){
            Category category = categoryRepository.findById(
                    updatedProduct.getCategory().getId()
            ).orElseThrow(() -> new RuntimeException("Category not found"));

            existing.setCategory(category);
        }
        // status
        existing.setStatus(updatedProduct.getStatus());

        //IMAGE UPLOAD

        if (files != null && !files.isEmpty()) {

            // xoá ảnh cũ
            if (files != null && files.stream().anyMatch(f -> !f.isEmpty())) {
                productImageRepository.deleteAll(existing.getImages());
                existing.getImages().clear();
            }

            int index = 0;

            for (MultipartFile file : files) {
                if (!file.isEmpty()) {

                    String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                    try {
                        java.nio.file.Path path = java.nio.file.Paths.get("uploads/" + fileName);
                        java.nio.file.Files.createDirectories(path.getParent());
                        java.nio.file.Files.write(path, file.getBytes());
                    } catch (Exception e) {
                        throw new RuntimeException("Upload fail");
                    }
                    ProductImage img = new ProductImage();
                    img.setImageUrl("/uploads/" + fileName);
                    img.setProduct(existing);
                    // set ảnh chính
                    if (mainIndex == null || mainIndex < 0 || mainIndex >= files.size()) {
                        mainIndex = 0;
                    }
                    productImageRepository.save(img);
                    index++;
                }
            }
        }
        productRepository.save(existing);
    }
}