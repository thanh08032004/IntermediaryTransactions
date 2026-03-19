package hsf302.group3.intermediarytransactions.repository;

import hsf302.group3.intermediarytransactions.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    Optional<Order> findByPaymentCode(String paymentCode);

    // =========================
    // BUY
    // =========================
    Page<Order> findByBuyer_Id(Integer buyerId, Pageable pageable);

    Page<Order> findByBuyer_IdAndStatus(
            Integer buyerId, Order.Status status, Pageable pageable);

    Page<Order> findByBuyer_IdAndOrderCodeContainingIgnoreCase(
            Integer buyerId, String keyword, Pageable pageable);

    Page<Order> findByBuyer_IdAndStatusAndOrderCodeContainingIgnoreCase(
            Integer buyerId, Order.Status status, String keyword, Pageable pageable);


    // =========================
    // SELL
    // =========================
    Page<Order> findBySeller_Id(Integer sellerId, Pageable pageable);

    Page<Order> findBySeller_IdAndStatus(
            Integer sellerId, Order.Status status, Pageable pageable);

    Page<Order> findBySeller_IdAndOrderCodeContainingIgnoreCase(
            Integer sellerId, String keyword, Pageable pageable);

    Page<Order> findBySeller_IdAndStatusAndOrderCodeContainingIgnoreCase(
            Integer sellerId, Order.Status status, String keyword, Pageable pageable);
}