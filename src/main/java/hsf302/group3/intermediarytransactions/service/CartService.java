package hsf302.group3.intermediarytransactions.service;

import hsf302.group3.intermediarytransactions.entity.Cart;
import hsf302.group3.intermediarytransactions.entity.CartItem;
import hsf302.group3.intermediarytransactions.entity.Product;
import hsf302.group3.intermediarytransactions.entity.User;
import hsf302.group3.intermediarytransactions.repository.CartItemRepository;
import hsf302.group3.intermediarytransactions.repository.CartRepository;
import hsf302.group3.intermediarytransactions.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    // Lấy giỏ hàng user, tạo mới nếu chưa có
    public Cart getCartByUser(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    return cartRepository.save(cart);
                });
    }
    @Transactional
    public void addToCart(User user, Integer productId, Integer quantity) {
        Cart cart = getCartByUser(user);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        if (product.getSupplier() != null
                && product.getSupplier().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không thể mua sản phẩm của chính mình!");
        }
        if (product.getAvailableQuantity() <= 0) {
            throw new RuntimeException("Sản phẩm đã hết hàng!");
        }

        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElse(null);

        int currentQuantity = (cartItem != null && cartItem.getQuantity() != null)
                ? cartItem.getQuantity()
                : 0;

        if (currentQuantity + quantity > product.getAvailableQuantity()) {
            throw new RuntimeException("Số lượng vượt quá tồn kho!");
        }

        if (cartItem == null) {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setPrice(product.getPrice());
            cartItem.setQuantity(quantity);
            cart.getItems().add(cartItem);
        } else {
            cartItem.setQuantity(currentQuantity + quantity);
        }

        cartRepository.save(cart);
    }
    @Transactional
    public void updateQuantity(User user, Integer productId, Integer quantity) {
        Cart cart = getCartByUser(user);
        Product product = productRepository.findById(productId).orElseThrow();
        cartItemRepository.findByCartAndProduct(cart, product).ifPresent(item -> {
            item.setQuantity(quantity);
            cartRepository.save(cart);
        });
    }

    @Transactional
    public void removeItem(User user, Integer productId) {
        Cart cart = getCartByUser(user);
        Product product = productRepository.findById(productId).orElseThrow();
        cart.getItems().removeIf(i -> i.getProduct().getId().equals(productId));
        cartRepository.save(cart);
    }
    public BigDecimal getTotal(User user) {
        Cart cart = getCartByUser(user);
        return cart.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}