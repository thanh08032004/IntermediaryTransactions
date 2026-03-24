package hsf302.group3.intermediarytransactions.service;

import hsf302.group3.intermediarytransactions.entity.*;
import hsf302.group3.intermediarytransactions.repository.OrderItemRepository;
import hsf302.group3.intermediarytransactions.repository.ProductItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final ProductItemRepository productItemRepository;
    private final OrderItemRepository orderItemRepository;

    public List<String> generateAccountsForOrderItem(OrderItem orderItem) {
        Product product = orderItem.getProduct();
        int quantity = orderItem.getQuantity() != null ? orderItem.getQuantity() : 0;

        List<ProductItem> availableItems = productItemRepository.findByProductAndStatus(product, ItemStatus.AVAILABLE);

        List<String> assignedAccounts = new ArrayList<>();
        for (int i = 0; i < quantity && i < availableItems.size(); i++) {
            ProductItem item = availableItems.get(i);
            item.setStatus(ItemStatus.SOLD);
            item.setBuyer(orderItem.getOrder().getBuyer());
            item.setSoldAt(LocalDateTime.now());

            assignedAccounts.add(item.getDescription());
            productItemRepository.save(item);
        }
        orderItemRepository.save(orderItem);

        return assignedAccounts;
    }
    public List<String> generateAccountsForOrder(Order order) {
        List<String> allAccounts = new ArrayList<>();
        for (OrderItem item : order.getOrderItems()) {
            allAccounts.addAll(generateAccountsForOrderItem(item));
        }
        return allAccounts;
    }
}