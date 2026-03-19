package hsf302.group3.intermediarytransactions.controller;

import hsf302.group3.intermediarytransactions.entity.Order;
import hsf302.group3.intermediarytransactions.entity.Product;
import hsf302.group3.intermediarytransactions.entity.User;
import hsf302.group3.intermediarytransactions.repository.OrderRepository;
import hsf302.group3.intermediarytransactions.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/market")
public class MarketController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    private Integer getUserId(HttpSession session) {
        return (Integer) session.getAttribute("userId");
    }

    // =========================
    // TRANG CHỢ (PRODUCT)
    // =========================
    @GetMapping("")
    public String market(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Pageable pageable = PageRequest.of(page, 6);

        Page<Product> productPage;

        if (minPrice != null && maxPrice != null) {
            productPage = productRepository.findByStatusAndPriceBetween(
                    Product.Status.ACTIVE, minPrice, maxPrice, pageable);

        } else if (minPrice != null) {
            productPage = productRepository.findByStatusAndPriceGreaterThanEqual(
                    Product.Status.ACTIVE, minPrice, pageable);

        } else if (maxPrice != null) {
            productPage = productRepository.findByStatusAndPriceLessThanEqual(
                    Product.Status.ACTIVE, maxPrice, pageable);

        } else {
            productPage = productRepository.findByStatus(
                    Product.Status.ACTIVE, pageable);
        }

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        return "market";
    }

    // =========================
    // ĐƠN MUA
    // =========================
    @GetMapping("/my-buy")
    public String myBuy(
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model,
            HttpSession session) {

        Integer userId = getUserId(session);
        Pageable pageable = PageRequest.of(page, 5);

        Page<Order> orderPage;

        if (!status.equalsIgnoreCase("ALL")) {

            Order.Status st = Order.Status.valueOf(status);

            if (!keyword.isEmpty()) {
                orderPage = orderRepository
                        .findByBuyer_IdAndStatusAndOrderCodeContainingIgnoreCase(
                                userId, st, keyword, pageable);
            } else {
                orderPage = orderRepository
                        .findByBuyer_IdAndStatus(userId, st, pageable);
            }

        } else {

            if (!keyword.isEmpty()) {
                orderPage = orderRepository
                        .findByBuyer_IdAndOrderCodeContainingIgnoreCase(
                                userId, keyword, pageable);
            } else {
                orderPage = orderRepository
                        .findByBuyer_Id(userId, pageable);
            }
        }

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentStatus", status);

        return "my-buy";
    }

    // =========================
    // ĐƠN BÁN
    // =========================
    @GetMapping("/my-sell")
    public String mySell(
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model,
            HttpSession session) {

        Integer userId = getUserId(session);
        Pageable pageable = PageRequest.of(page, 5);

        Page<Order> orderPage;

        if (!status.equalsIgnoreCase("ALL")) {

            Order.Status st = Order.Status.valueOf(status);

            if (!keyword.isEmpty()) {
                orderPage = orderRepository
                        .findBySeller_IdAndStatusAndOrderCodeContainingIgnoreCase(
                                userId, st, keyword, pageable);
            } else {
                orderPage = orderRepository
                        .findBySeller_IdAndStatus(userId, st, pageable);
            }

        } else {

            if (!keyword.isEmpty()) {
                orderPage = orderRepository
                        .findBySeller_IdAndOrderCodeContainingIgnoreCase(
                                userId, keyword, pageable);
            } else {
                orderPage = orderRepository
                        .findBySeller_Id(userId, pageable);
            }
        }

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentStatus", status);

        return "my-sell";
    }

    // =========================
    // MUA HÀNG
    // =========================
    @PostMapping("/buy/{id}")
    public String buyOrder(@PathVariable Integer id, HttpSession session) {

        Integer userId = getUserId(session);
        if (userId == null) return "redirect:/login";

        Order order = orderRepository.findById(id).orElse(null);

        if (order != null && order.getStatus() == Order.Status.PENDING) {

            // không cho tự mua của mình
            if (userId.equals(order.getSeller().getId())) {
                return "redirect:/market";
            }

            User buyer = new User();
            buyer.setId(userId);

            order.setBuyer(buyer);
            order.setStatus(Order.Status.PROCESSING);

            orderRepository.save(order);
        }

        return "redirect:/market/my-buy";
    }

    // =========================
    // CHI TIẾT
    // =========================
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Integer id, Model model) {

        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) return "redirect:/market";

        model.addAttribute("order", order);
        return "order-detail";
    }

    // =========================
    // HỦY
    // =========================
    @PostMapping("/cancel/{id}")
    public String cancelOrder(@PathVariable Integer id) {

        Order order = orderRepository.findById(id).orElse(null);

        if (order != null) {
            order.setStatus(Order.Status.CANCELLED);
            orderRepository.save(order);
        }

        return "redirect:/market/my-buy";
    }

    // =========================
    // HOÀN THÀNH
    // =========================
    @PostMapping("/complete/{id}")
    public String complete(@PathVariable Integer id, HttpSession session) {

        Integer userId = getUserId(session);
        Order order = orderRepository.findById(id).orElse(null);

        if (order != null && userId != null
                && userId.equals(order.getSeller().getId())) {

            order.setStatus(Order.Status.COMPLETED);
            orderRepository.save(order);
        }

        return "redirect:/market/my-sell";
    }
}