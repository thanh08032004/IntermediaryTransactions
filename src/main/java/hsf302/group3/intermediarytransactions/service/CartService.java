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
    // Thêm sản phẩm vào giỏ
    @Transactional
    public void addToCart(User user, Integer productId, Integer quantity) {
        Cart cart = getCartByUser(user);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Kiểm tra nếu sản phẩm đã có trong giỏ
        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseGet(() -> {
                    CartItem item = new CartItem();
                    item.setCart(cart);
                    item.setProduct(product);
                    item.setPrice(product.getPrice());
                    cart.getItems().add(item);
                    return item;
                });

        cartItem.setQuantity(cartItem.getQuantity() != null ? cartItem.getQuantity() + quantity : quantity);

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