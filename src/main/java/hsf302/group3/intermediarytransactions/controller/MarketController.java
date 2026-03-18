package hsf302.group3.intermediarytransactions.controller;

import hsf302.group3.intermediarytransactions.entity.Order;
import hsf302.group3.intermediarytransactions.repository.OrderRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/market")
public class MarketController {

    @Autowired
    private OrderRepository orderRepository;

    private Integer getUserId(HttpSession session) {
        return (Integer) session.getAttribute("userId");
    }

    // =========================
    // TRANG CHỢ (FILTER + PAGINATION)
    // =========================
    @GetMapping("")
    public String market(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        int size = 6;
        Pageable pageable = PageRequest.of(page, size);

        Page<Order> orderPage;

        if (minPrice != null && maxPrice != null) {
            orderPage = orderRepository
                    .findByStatusAndTotalAmountBetween("PENDING", minPrice, maxPrice, pageable);

        } else if (minPrice != null) {
            orderPage = orderRepository
                    .findByStatusAndTotalAmountGreaterThanEqual("PENDING", minPrice, pageable);

        } else if (maxPrice != null) {
            orderPage = orderRepository
                    .findByStatusAndTotalAmountLessThanEqual("PENDING", maxPrice, pageable);

        } else {
            orderPage = orderRepository
                    .findByStatus("PENDING", pageable);
        }

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());

        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        return "market";
    }

    // =========================
    // ĐƠN MUA (SEARCH + PAGINATION)
    // =========================
    @GetMapping("/my-buy")
    public String myBuy(
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model,
            HttpSession session) {

        Integer userId = getUserId(session);

//        if (userId == null) {
//            return "redirect:/login";
//        }

        int size = 5;
        Pageable pageable = PageRequest.of(page, size);

        Page<Order> orderPage;

        if (!status.equalsIgnoreCase("ALL")) {

            if (!keyword.isEmpty()) {
                orderPage = orderRepository
                        .findByBuyerIdAndStatusAndOrderCodeContainingIgnoreCase(
                                userId, status, keyword, pageable);
            } else {
                orderPage = orderRepository
                        .findByBuyerIdAndStatus(userId, status, pageable);
            }

        } else {

            if (!keyword.isEmpty()) {
                orderPage = orderRepository
                        .findByBuyerIdAndOrderCodeContainingIgnoreCase(
                                userId, keyword, pageable);
            } else {
                orderPage = orderRepository
                        .findByBuyerId(userId, pageable);
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
    // ĐƠN BÁN (SEARCH + PAGINATION)
    // =========================
    @GetMapping("/my-sell")
    public String mySell(
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model,
            HttpSession session) {

        Integer userId = getUserId(session);

//        if (userId == null) {
//            return "redirect:/login";
//        }

        int size = 5;
        Pageable pageable = PageRequest.of(page, size);

        Page<Order> orderPage;

        if (!status.equalsIgnoreCase("ALL")) {

            if (!keyword.isEmpty()) {
                orderPage = orderRepository
                        .findByUserIdAndStatusAndOrderCodeContainingIgnoreCase(
                                userId, status, keyword, pageable);
            } else {
                orderPage = orderRepository
                        .findByUserIdAndStatus(userId, status, pageable);
            }

        } else {

            if (!keyword.isEmpty()) {
                orderPage = orderRepository
                        .findByUserIdAndOrderCodeContainingIgnoreCase(
                                userId, keyword, pageable);
            } else {
                orderPage = orderRepository
                        .findByUserId(userId, pageable);
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

        if (userId == null) {
            return "redirect:/login";
        }

        Order order = orderRepository.findById(id).orElse(null);

        if (order != null && "PENDING".equals(order.getStatus())) {

            if (userId.equals(order.getUserId())) {
                return "redirect:/market";
            }

            order.setBuyerId(userId);
            order.setStatus("PROCESSING");
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

        if (order == null) {
            return "redirect:/market";
        }

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
            order.setStatus("CANCELLED");
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
                && userId.equals(order.getUserId())) {

            order.setStatus("COMPLETED");
            orderRepository.save(order);
        }

        return "redirect:/market/my-sell";
    }
}