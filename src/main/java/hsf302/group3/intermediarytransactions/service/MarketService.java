package hsf302.group3.intermediarytransactions.service;

import hsf302.group3.intermediarytransactions.entity.Order;
import hsf302.group3.intermediarytransactions.entity.User;
import hsf302.group3.intermediarytransactions.repository.OrderRepository;
import hsf302.group3.intermediarytransactions.util.constant.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class MarketService {

    private final OrderRepository orderRepository;

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

        if (status != null && !status.equalsIgnoreCase("ALL") && !status.isEmpty()) {
            OrderStatus st = OrderStatus.valueOf(status);

            if (keyword != null && !keyword.isEmpty()) {
                return orderRepository
                        .findByBuyerIdAndStatusAndOrderCodeContainingIgnoreCase(userId, st, keyword, pageable);
            }
            return orderRepository.findByBuyerIdAndStatus(userId, st, pageable);
        }

        if (keyword != null && !keyword.isEmpty()) {
            return orderRepository.findByBuyerIdAndOrderCodeContainingIgnoreCase(userId, keyword, pageable);
        }

        return orderRepository.findByBuyerId(userId, pageable);
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

    // BUY ACTION
    public void buyOrder(Integer orderId, User user) {

        Order order = orderRepository.findById(orderId).orElseThrow();

        if (order.getStatus() == OrderStatus.PENDING &&
                (order.getBuyer() == null || !order.getBuyer().getId().equals(user.getId()))) {

            order.setBuyer(user);
            order.setStatus(OrderStatus.PROCESSING);
            orderRepository.save(order);
        }
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

    public Order getOrderDetail(Integer id) {
        return orderRepository.findById(id).orElse(null);
    }

}
