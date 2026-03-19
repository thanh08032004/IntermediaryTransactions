package hsf302.group3.intermediarytransactions.controller;

import hsf302.group3.intermediarytransactions.entity.Cart;
import hsf302.group3.intermediarytransactions.entity.User;
import hsf302.group3.intermediarytransactions.security.CustomUserDetails;
import hsf302.group3.intermediarytransactions.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    public String addToCart(@AuthenticationPrincipal CustomUserDetails userDetails,
                            @RequestParam Integer productId,
                            @RequestParam(defaultValue = "1") Integer quantity) {
        User user = userDetails.getUser();
        cartService.addToCart(user, productId, quantity);
        return "redirect:/cart"; // chuyển sang trang giỏ hàng
    }

    @GetMapping
    public String viewCart(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser();
        Cart cart = cartService.getCartByUser(user);

        model.addAttribute("cartItems", cart.getItems());
        model.addAttribute("total", cart.getItems().stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        return "cart"; // Thymeleaf template: cart.html
    }

    @PostMapping("/update")
    public String updateCart(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @RequestParam Integer productId,
                             @RequestParam Integer quantity) {
        User user = userDetails.getUser();
        cartService.updateQuantity(user, productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeFromCart(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @RequestParam Integer productId) {
        User user = userDetails.getUser();
        cartService.removeItem(user, productId);
        return "redirect:/cart";
    }
}