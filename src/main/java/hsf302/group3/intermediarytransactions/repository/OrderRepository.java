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
    // MARKET (ĐÃ CÓ)
    // =========================
    Page<Order> findByStatus(String status, Pageable pageable);

    Page<Order> findByStatusAndTotalAmountBetween(
            String status, Double min, Double max, Pageable pageable);

    Page<Order> findByStatusAndTotalAmountGreaterThanEqual(
            String status, Double min, Pageable pageable);

    Page<Order> findByStatusAndTotalAmountLessThanEqual(
            String status, Double max, Pageable pageable);

    List<Order> findByStatus(String status);

    // =========================
    // BUY (CŨ)
    // =========================
    List<Order> findByBuyerId(Integer buyerId);

    // =========================
    // SELL (CŨ)
    // =========================
    List<Order> findByUserId(Integer userId);


    // =========================
    // BUY (SEARCH + PAGINATION)
    // =========================
    Page<Order> findByBuyerId(Integer buyerId, Pageable pageable);

    Page<Order> findByBuyerIdAndStatus(
            Integer buyerId, String status, Pageable pageable);

    Page<Order> findByBuyerIdAndOrderCodeContainingIgnoreCase(
            Integer buyerId, String keyword, Pageable pageable);

    Page<Order> findByBuyerIdAndStatusAndOrderCodeContainingIgnoreCase(
            Integer buyerId, String status, String keyword, Pageable pageable);


    // =========================
    // SELL (SEARCH + PAGINATION)
    // =========================
    Page<Order> findByUserId(Integer userId, Pageable pageable);

    Page<Order> findByUserIdAndStatus(
            Integer userId, String status, Pageable pageable);

    Page<Order> findByUserIdAndOrderCodeContainingIgnoreCase(
            Integer userId, String keyword, Pageable pageable);

    Page<Order> findByUserIdAndStatusAndOrderCodeContainingIgnoreCase(
            Integer userId, String status, String keyword, Pageable pageable);
}