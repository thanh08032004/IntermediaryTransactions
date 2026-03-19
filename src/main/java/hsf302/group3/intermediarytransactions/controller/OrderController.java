package hsf302.group3.intermediarytransactions.controller;


import hsf302.group3.intermediarytransactions.entity.Order;
import hsf302.group3.intermediarytransactions.entity.User;
import hsf302.group3.intermediarytransactions.service.CartService;
import hsf302.group3.intermediarytransactions.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
public class OrderController {

    private final CartService cartService;
    private final OrderService orderService;

    // Trang nhập thông tin
    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_ORDER')")
    public String checkoutPage(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("cart", cartService.getCartByUser(user));
        model.addAttribute("total", cartService.getTotal(cartService.getCartByUser(user)));
        return "order-add";
    }

    // Submit order
    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_ORDER')")
    public String placeOrder(@AuthenticationPrincipal User user) {

        Order order = orderService.createOrderFromCart(user);

        return "redirect:/orders/success?code=" + order.getOrderCode();
    }

    // success page (optional)
    @GetMapping("/success")
    @PreAuthorize("hasAuthority('MANAGE_ORDER')")
    public String success(@RequestParam String code, Model model) {
        model.addAttribute("code", code);
        return "order-success";
    }
}