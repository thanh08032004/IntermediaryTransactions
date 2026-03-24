package hsf302.group3.intermediarytransactions.controller;

import hsf302.group3.intermediarytransactions.entity.Order;
import hsf302.group3.intermediarytransactions.entity.User;
import hsf302.group3.intermediarytransactions.repository.OrderRepository;
import hsf302.group3.intermediarytransactions.repository.UserRepository;
import hsf302.group3.intermediarytransactions.service.MarketService;
import hsf302.group3.intermediarytransactions.util.SecurityUtil;
import hsf302.group3.intermediarytransactions.util.constant.OrderStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/market")
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
@RequiredArgsConstructor
public class MarketController {

    private final MarketService marketService;
    private final UserRepository userRepository;

    private Integer getUserId() {
        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsername(username).orElse(null);
        return user.getId();
    }
    // MY BUY
    @GetMapping("/my-buy")
    @PreAuthorize("hasAuthority('MANAGE_ORDER')")
    public String myBuy(
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Integer userId = getUserId();

        if (userId == null) {
            return "redirect:/login";
        }

        Pageable pageable = PageRequest.of(page, 5);

        Page<Order> orderPage =
                marketService.getMyBuy(userId, status, keyword, pageable);

        model.addAttribute("currentStatus", status);
        model.addAttribute("keyword", keyword);

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());

        return "my-buy";
    }

    @GetMapping("/order-history")
    public String myBuyOrders(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model,
            HttpServletRequest request) {

        Integer userId = getUserId();
        int size = 5;

        Page<Order> orderPage = marketService.getMyPaidOrders(userId, keyword, page, size);
        List<Order> completedOrders = orderPage.getContent().stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .toList();

        model.addAttribute("orders", completedOrders);
        int totalPages = marketService.countMyPaidOrderPages(userId, keyword, size);

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentUri", request.getRequestURI());

        return "order-history";
    }
    @PostMapping("/pay/balance/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ORDER')")
    public String payWithBalance(@PathVariable Integer id) {

        Integer userId = getUserId();
        marketService.payWithBalance(id, userId);

        return "redirect:/market/my-buy";
    }
    @PostMapping("/buy/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ORDER')")
    public String buy(@PathVariable Integer id) {

        Integer userId = getUserId();
        User user = userRepository.findById(userId).orElse(null);

        marketService.buyOrder(id, user);

        return "redirect:/market/my-buy";
    }

    // CANCEL
    @PostMapping("/cancel/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ORDER')")
    public String cancel(@PathVariable Integer id) {
        marketService.cancelOrder(id);
        return "redirect:/market/my-buy";
    }

    // COMPLETE
    @PostMapping("/complete/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ORDER')")
    public String complete(@PathVariable Integer id) {
        marketService.completeOrder(id, getUserId());
        return "redirect:/market/my-sell";
    }

    // DETAIL
    @GetMapping("/detail/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ORDER')")
    public String detail(@PathVariable Integer id, Model model) {

        Order order = marketService.getOrderDetail(id);

        if (order == null) {
            return "redirect:/market/my-buy";
        }

        model.addAttribute("order", order);

        return "order-detail";
    }

}