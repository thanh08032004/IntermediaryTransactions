package hsf302.group3.intermediarytransactions.controller;

import hsf302.group3.intermediarytransactions.entity.Cart;
import hsf302.group3.intermediarytransactions.entity.User;
import hsf302.group3.intermediarytransactions.service.CartService;
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
@RequestMapping("/cart")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_CART')")
    public String viewCart(Model model, @AuthenticationPrincipal User user) {
        Cart cart = cartService.getCartByUser(user);
        model.addAttribute("cartItems", cart.getItems());
        model.addAttribute("total", cartService.getTotal(cart));
        return "cart"; // cart.html
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('MANAGE_CART')")
    public String addToCart(@AuthenticationPrincipal User user,
                            @RequestParam Integer productId,
                            @RequestParam(defaultValue = "1") int quantity) {
        cartService.addToCart(user, productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/update")
    @PreAuthorize("hasAuthority('MANAGE_CART')")
    public String updateQuantity(@AuthenticationPrincipal User user,
                                 @RequestParam Integer productId,
                                 @RequestParam int quantity) {
        cartService.updateQuantity(user, productId, quantity);

        Cart cart = cartService.getCartByUser(user);
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    @PreAuthorize("hasAuthority('MANAGE_CART')")
    public String removeItem(@AuthenticationPrincipal User user,
                             @RequestParam Integer productId) {
        cartService.removeFromCart(user, productId);
        return "redirect:/cart";
    }
}
