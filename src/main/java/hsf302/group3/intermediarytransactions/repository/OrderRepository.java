package hsf302.group3.intermediarytransactions.repository;

import hsf302.group3.intermediarytransactions.entity.Order;
import hsf302.group3.intermediarytransactions.entity.User;
import hsf302.group3.intermediarytransactions.util.constant.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    Optional<Order> findByPaymentCode(String paymentCode);

    Order findByOrderCode(String orderCode);

    List<Order> findByBuyer(User buyer);

    // ================= MARKET =================
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Page<Order> findByStatusAndTotalAmountBetween(
            OrderStatus status, BigDecimal min, BigDecimal max, Pageable pageable);

    Page<Order> findByStatusAndTotalAmountGreaterThanEqual(
            OrderStatus status, BigDecimal min, Pageable pageable);

    Page<Order> findByStatusAndTotalAmountLessThanEqual(
            OrderStatus status, BigDecimal max, Pageable pageable);

    // ================= BUY =================
    Page<Order> findByBuyerId(Integer buyerId, Pageable pageable);

    Page<Order> findByBuyerIdAndStatus(
            Integer buyerId, OrderStatus status, Pageable pageable);

    Page<Order> findByBuyerIdAndOrderCodeContainingIgnoreCase(
            Integer buyerId, String keyword, Pageable pageable);

    Page<Order> findByBuyerIdAndStatusAndOrderCodeContainingIgnoreCase(
            Integer buyerId, OrderStatus status, String keyword, Pageable pageable);

    // ================= SELL =================
    Page<Order> findBySellerId(Integer sellerId, Pageable pageable);

    Page<Order> findBySellerIdAndStatus(
            Integer sellerId, OrderStatus status, Pageable pageable);

    Page<Order> findBySellerIdAndOrderCodeContainingIgnoreCase(
            Integer sellerId, String keyword, Pageable pageable);

    Page<Order> findBySellerIdAndStatusAndOrderCodeContainingIgnoreCase(
            Integer sellerId, OrderStatus status, String keyword, Pageable pageable);
}