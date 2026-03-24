package hsf302.group3.intermediarytransactions.service;

import hsf302.group3.intermediarytransactions.entity.*;
import hsf302.group3.intermediarytransactions.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.List;

@Service
public class ProductSellerService {

    @Autowired
    private ProductRepository productRepository;

    // 👉 folder lưu ảnh ngoài project
    private final String UPLOAD_DIR = "uploads/";

    // ================= CREATE =================
    public Product createWithImages(Product product, Integer sellerId,
                                    MultipartFile[] images, Integer mainIndex) {

        product.setId(null);
        product.setSupplier(new User(sellerId));
        product.setStatus(ProductStatus.ACTIVE);

        Product saved = productRepository.save(product);

        if (images != null && images.length > 0) {
            for (int i = 0; i < images.length; i++) {
                MultipartFile file = images[i];

                if (!file.isEmpty()) {
                    try {
                        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();

                        Path uploadPath = Paths.get(UPLOAD_DIR);
                        Files.createDirectories(uploadPath);

                        Path filePath = uploadPath.resolve(filename);
                        Files.write(filePath, file.getBytes());

                        ProductImage img = new ProductImage();
                        img.setProduct(saved);
                        img.setImageUrl("/images/" + filename); // URL
                        img.setIsMain(mainIndex != null && i == mainIndex);

                        saved.getImages().add(img);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            productRepository.save(saved);
        }

        return saved;
    }

    // ================= LIST =================
    public Page<Product> getSellerProducts(Integer sellerId, String keyword, Pageable pageable) {
        return productRepository.searchBySeller(sellerId, keyword, pageable);
    }

    // ================= GET =================
    public Product getById(Integer id, Integer sellerId) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!p.getSupplier().getId().equals(sellerId)) {
            throw new RuntimeException("Access denied");
        }

        return p;
    }

    // ================= UPDATE SIMPLE =================
    public Product update(Integer id, Product updated, Integer sellerId) {
        Product old = getById(id, sellerId);

        old.setName(updated.getName());
        old.setPrice(updated.getPrice());
        old.setDescription(updated.getDescription());
        old.setCategory(updated.getCategory());
        old.setStatus(updated.getStatus());

        return productRepository.save(old);
    }

    // ================= UPDATE FULL (IMAGE) =================
    public Product update(Integer id,
                          Product updated,
                          Integer sellerId,
                          MultipartFile[] files,
                          List<Integer> deleteImageIds) {

        Product old = getById(id, sellerId);

        old.setName(updated.getName());
        old.setPrice(updated.getPrice());
        old.setDescription(updated.getDescription());
        old.setCategory(updated.getCategory());
        old.setStatus(updated.getStatus());

        // ===== DELETE IMAGE =====
        if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
            old.getImages().removeIf(img -> {
                if (deleteImageIds.contains(img.getId())) {
                    // xoá file vật lý
                    try {
                        String filename = img.getImageUrl().replace("/images/", "");
                        Path filePath = Paths.get(UPLOAD_DIR).resolve(filename);
                        Files.deleteIfExists(filePath);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                }
                return false;
            });
        }

        // ===== ADD NEW IMAGE =====
        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    try {
                        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();

                        Path uploadPath = Paths.get(UPLOAD_DIR);
                        Files.createDirectories(uploadPath);

                        Path filePath = uploadPath.resolve(filename);
                        Files.write(filePath, file.getBytes());

                        ProductImage img = new ProductImage();
                        img.setProduct(old);
                        img.setImageUrl("/images/" + filename);
                        img.setIsMain(false);

                        old.getImages().add(img);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // ===== đảm bảo có main image =====
        if (old.getImages().stream().noneMatch(ProductImage::getIsMain)) {
            if (!old.getImages().isEmpty()) {
                old.getImages().get(0).setIsMain(true);
            }
        }

        return productRepository.save(old);
    }

    // ================= DELETE =================
    public void delete(Integer id, Integer sellerId) {
        Product p = getById(id, sellerId);

        // xoá file ảnh
        if (p.getImages() != null) {
            for (ProductImage img : p.getImages()) {
                try {
                    String filename = img.getImageUrl().replace("/images/", "");
                    Path filePath = Paths.get(UPLOAD_DIR).resolve(filename);
                    Files.deleteIfExists(filePath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        productRepository.delete(p);
    }
}