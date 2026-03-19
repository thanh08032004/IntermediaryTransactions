package hsf302.group3.intermediarytransactions.controller;

import hsf302.group3.intermediarytransactions.entity.Order;
import hsf302.group3.intermediarytransactions.entity.User;
import hsf302.group3.intermediarytransactions.repository.OrderRepository;
import hsf302.group3.intermediarytransactions.repository.UserRepository;
import hsf302.group3.intermediarytransactions.service.MarketService;
import hsf302.group3.intermediarytransactions.util.SecurityUtil;
import hsf302.group3.intermediarytransactions.util.constant.OrderStatus;
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

    // MARKET
//    @GetMapping("")
//    public String market(Double minPrice, Double maxPrice,
//                         @RequestParam(defaultValue = "0") int page,
//                         Model model) {
//
//        Pageable pageable = PageRequest.of(page, 6);
//
//        Page<Order> orderPage = marketService.getMarketOrders(minPrice, maxPrice, pageable);
//
//        model.addAttribute("orders", orderPage.getContent());
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", orderPage.getTotalPages());
//
//        return "market";
//    }

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

    // MY SELL
    @GetMapping("/my-sell")
    @PreAuthorize("hasAuthority('MANAGE_ORDER')")
    public String mySell(
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
                marketService.getMySell(userId, status, keyword, pageable);

        model.addAttribute("currentStatus", status);
        model.addAttribute("keyword", keyword);

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());

        return "my-sell";
    }

    // BUY
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