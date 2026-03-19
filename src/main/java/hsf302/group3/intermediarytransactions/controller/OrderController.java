package hsf302.group3.intermediarytransactions.controller;

import hsf302.group3.intermediarytransactions.entity.Cart;
import hsf302.group3.intermediarytransactions.entity.Order;
import hsf302.group3.intermediarytransactions.entity.User;
import hsf302.group3.intermediarytransactions.security.CustomUserDetails;
import hsf302.group3.intermediarytransactions.service.CartService;
import hsf302.group3.intermediarytransactions.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

import java.math.BigDecimal;

@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class OrderController {

    private final CartService cartService;
    private final OrderService orderService;

    // GET /checkout
    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_ORDER')")
    public String checkoutPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        CustomUserDetails custom = (CustomUserDetails) userDetails; // ép kiểu
        User user = custom.getUser();

        Cart cart = cartService.getCartByUser(user);
        BigDecimal total = cartService.getTotal(user);

        model.addAttribute("cart", cart);
        model.addAttribute("total", total);

        return "create-order";
    }

    // POST /checkout/confirm
    @PostMapping("/confirm")
    public String confirmOrder(@AuthenticationPrincipal UserDetails userDetails,
                               Model model,
                               SessionStatus status) {
        CustomUserDetails custom = (CustomUserDetails) userDetails;
        User user = custom.getUser();

        Cart cart = cartService.getCartByUser(user);
        if (cart == null || cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        Order order = orderService.createOrderFromCart(user);

        model.addAttribute("order", order);
        model.addAttribute("cartItems", order.getOrderItems());
        model.addAttribute("total", order.getTotalAmount());

        status.setComplete();
        return "create-order";
    }
}