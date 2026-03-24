package hsf302.group3.intermediarytransactions.service;

import hsf302.group3.intermediarytransactions.entity.*;
import hsf302.group3.intermediarytransactions.repository.OrderItemProductItemRepository;
import hsf302.group3.intermediarytransactions.repository.ProductItemRepository;
import hsf302.group3.intermediarytransactions.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductItemServiceImpl implements ProductItemService {

    @Autowired
    private ProductItemRepository productItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderItemProductItemRepository orderItemProductItemRepository;
    @Override
    public List<ProductItem> findByProductId(Integer productId) {
        return productItemRepository.findByProductId(productId);
    }

    @Override
    public List<ProductItem> getAvailableItems(Integer productId) {
        return List.of();
    }

    @Override
    public ProductItem save(ProductItem item) {
        return null;
    }

    @Override
    public void delete(Integer id) {

    }

    @Override
    public ProductItem addItem(Integer productId, ProductItem item) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        item.setProduct(product);            // gán product managed
        item.setStatus(ItemStatus.AVAILABLE); // mặc định AVAILABLE khi thêm mới
        return productItemRepository.save(item); // save và return
    }

    @Override
    public List<ProductItem> getAvailableItems(Integer productId, int quantity) {
        List<ProductItem> items = productItemRepository.findByProductIdAndStatus(productId, ItemStatus.AVAILABLE);
        if (items.size() < quantity) {
            throw new RuntimeException("Not enough product items available");
        }
        return items.subList(0, quantity);
    }

    @Override
    public void assignToOrderItem(OrderItem orderItem, List<ProductItem> items) {
        for (ProductItem item : items) {
            item.setStatus(ItemStatus.SOLD);
            item.setBuyer(orderItem.getOrder().getBuyer());
            item.setSoldAt(LocalDateTime.now());
            productItemRepository.save(item);

            OrderItemDetail link = OrderItemDetail.builder()
                    .orderItem(orderItem)
                    .productItem(item)
                    .build();
            orderItemProductItemRepository.save(link);
        }
    }
}