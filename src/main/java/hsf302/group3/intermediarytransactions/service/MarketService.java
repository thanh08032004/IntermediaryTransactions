package hsf302.group3.intermediarytransactions.service;

import hsf302.group3.intermediarytransactions.entity.*;
import hsf302.group3.intermediarytransactions.repository.OrderItemProductItemRepository;
import hsf302.group3.intermediarytransactions.repository.OrderRepository;
import hsf302.group3.intermediarytransactions.repository.ProductItemRepository;
import hsf302.group3.intermediarytransactions.repository.WalletRepository;
import hsf302.group3.intermediarytransactions.util.constant.OrderStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketService {

    private final OrderRepository orderRepository;
    private final WalletRepository walletRepository;
    private final ProductItemService productItemService;
    private final ProductItemRepository productItemRepository;
    private final OrderItemProductItemRepository orderItemProductItemRepository;

    // MARKET
    public Page<Order> getMarketOrders(Double min, Double max, Pageable pageable) {

        if (min != null && max != null) {
            return orderRepository.findByStatusAndTotalAmountBetween(
                    OrderStatus.PENDING,
                    BigDecimal.valueOf(min),
                    BigDecimal.valueOf(max),
                    pageable);
        }

        if (min != null) {
            return orderRepository.findByStatusAndTotalAmountGreaterThanEqual(
                    OrderStatus.PENDING,
                    BigDecimal.valueOf(min),
                    pageable);
        }

        if (max != null) {
            return orderRepository.findByStatusAndTotalAmountLessThanEqual(
                    OrderStatus.PENDING,
                    BigDecimal.valueOf(max),
                    pageable);
        }

        return orderRepository.findByStatus(OrderStatus.PENDING, pageable);
    }

    // BUY
    public Page<Order> getMyBuy(Integer userId, String status, String keyword, Pageable pageable) {

        // Nếu người dùng chọn một status cụ thể
        if (status != null && !status.equalsIgnoreCase("ALL") && !status.isEmpty()) {
            OrderStatus st = OrderStatus.valueOf(status);

            if (keyword != null && !keyword.isEmpty()) {
                return orderRepository
                        .findByBuyerIdAndStatusAndOrderCodeContainingIgnoreCase(userId, st, keyword, pageable);
            }
            return orderRepository.findByBuyerIdAndStatus(userId, st, pageable);
        }

        // status = ALL → muốn lấy tất cả ngoại trừ COMPLETED
        if (keyword != null && !keyword.isEmpty()) {
            return orderRepository.findByBuyerIdAndStatusNotAndOrderCodeContainingIgnoreCase(
                    userId, OrderStatus.COMPLETED, keyword, pageable);
        }

        return orderRepository.findByBuyerIdAndStatusNot(userId, OrderStatus.COMPLETED, pageable);
    }

    // SELL
    public Page<Order> getMySell(Integer userId, String status, String keyword, Pageable pageable) {

        if (status != null && !status.equalsIgnoreCase("ALL")) {
            OrderStatus st = OrderStatus.valueOf(status);

            if (!keyword.isEmpty()) {
                return orderRepository
                        .findBySellerIdAndStatusAndOrderCodeContainingIgnoreCase(userId, st, keyword, pageable);
            }
            return orderRepository.findBySellerIdAndStatus(userId, st, pageable);
        }

        if (!keyword.isEmpty()) {
            return orderRepository.findBySellerIdAndOrderCodeContainingIgnoreCase(userId, keyword, pageable);
        }

        return orderRepository.findBySellerId(userId, pageable);
    }

    public void cancelOrder(Integer id) {
        Order order = orderRepository.findById(id).orElseThrow();
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    public void completeOrder(Integer id, Integer userId) {
        Order order = orderRepository.findById(id).orElseThrow();

        if (order.getSeller() != null &&
                order.getSeller().getId().equals(userId)) {

            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);
        }
    }
    @Transactional
    public void payWithBalance(Integer orderId, Integer userId) {
        // Lấy đơn hàng
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Kiểm tra buyer
        if (order.getBuyer() == null || !order.getBuyer().getId().equals(userId)) {
            throw new RuntimeException("Not your order");
        }

        // Chỉ cho thanh toán khi order đang PROCESSING hoặc PENDING
        if (order.getStatus() != OrderStatus.PROCESSING && order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Order not ready to pay");
        }

        // Lấy ví buyer và seller
        Wallet buyerWallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        Wallet sellerWallet = walletRepository.findByUserId(order.getSeller().getId())
                .orElseThrow(() -> new RuntimeException("Seller wallet not found"));

        BigDecimal total = order.getTotalAmount();
        if (buyerWallet.getBalance().compareTo(total) < 0) {
            order.setPaymentStatus(PaymentStatus.FAILED);
            orderRepository.save(order);
            throw new RuntimeException("Insufficient balance");
        }

        // Trừ tiền buyer, cộng tiền seller
        buyerWallet.setBalance(buyerWallet.getBalance().subtract(total));
        sellerWallet.setBalance(sellerWallet.getBalance().add(total));
        walletRepository.save(buyerWallet);
        walletRepository.save(sellerWallet);

        // **Gán sản phẩm cho buyer và lưu OrderItemDetail**
        for (OrderItem item : order.getOrderItems()) {
            for (int i = 0; i < item.getQuantity(); i++) {
                ProductItem productItem = productItemRepository
                        .findFirstByProductAndBuyerIsNull(item.getProduct())
                        .orElseThrow(() -> new RuntimeException("Not enough product items"));

                // Cập nhật ProductItem
                productItem.setBuyer(order.getBuyer());
                productItem.setStatus(ItemStatus.SOLD);
                productItem.setSoldAt(LocalDateTime.now());
                productItemRepository.save(productItem);

                // Tạo OrderItemDetail
                OrderItemDetail detail = OrderItemDetail.builder()
                        .orderItem(item)
                        .productItem(productItem)
                        .build();
                orderItemProductItemRepository.save(detail);
            }
        }

        // Cập nhật trạng thái order
        order.setStatus(OrderStatus.COMPLETED);
        order.setPaymentStatus(PaymentStatus.SUCCESS);
        orderRepository.save(order);
    }
    public Order getOrderDetail(Integer id) {
        return orderRepository.findById(id).orElse(null);
    }
    public Page<Order> getMyPaidOrders(Integer userId, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        if (keyword != null && !keyword.isEmpty()) {
            return orderRepository.findByBuyerIdAndStatusAndOrderCodeContainingIgnoreCase(
                    userId, OrderStatus.COMPLETED, keyword, pageable);
        }

        return orderRepository.findByBuyerIdAndStatus(userId, OrderStatus.COMPLETED, pageable);
    }
    @Transactional
    public Order getOrderById(Integer orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }
    public int countMyPaidOrderPages(Integer userId, String keyword, int size) {
        long totalItems;

        if (keyword != null && !keyword.isEmpty()) {
            totalItems = orderRepository.countByBuyerIdAndStatusAndOrderCodeContainingIgnoreCase(
                    userId, OrderStatus.COMPLETED, keyword);
        } else {
            totalItems = orderRepository.countByBuyerIdAndStatus(userId, OrderStatus.COMPLETED);
        }

        return (int) Math.ceil((double) totalItems / size);
    }
    @Transactional
    public void buyOrder(Integer orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Order cannot be bought");
        }

        order.setBuyer(user);
        order.setStatus(OrderStatus.PROCESSING);

        for (OrderItem orderItem : order.getOrderItems()) {
            int quantity = orderItem.getQuantity();
            List<ProductItem> availableItems = productItemService.getAvailableItems(orderItem.getProduct().getId(), quantity);

            productItemService.assignToOrderItem(orderItem, availableItems);
        }

        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);
    }
}
