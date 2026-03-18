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

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository; // để lấy Product theo id

    // Lấy giỏ hàng của user, nếu chưa có thì tạo mới
    public Cart getCartByUser(User user) {
        return cartRepository.findByUser(user).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUser(user);
            return cartRepository.save(cart);
        });
    }

    // Thêm sản phẩm vào giỏ
    public Cart addToCart(User user, Integer productId, int quantity) {
        Cart cart = getCartByUser(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElse(new CartItem());

        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setPrice(product.getPrice());

        if (cartItem.getId() != null) {
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        } else {
            cartItem.setQuantity(quantity);
            cart.getItems().add(cartItem);
        }

        cartRepository.save(cart);
        return cart;
    }

    // Xóa sản phẩm khỏi giỏ
    public void removeFromCart(User user, Integer productId) {
        Cart cart = getCartByUser(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        cartItemRepository.findByCartAndProduct(cart, product).ifPresent(cartItem -> {
            cart.getItems().remove(cartItem);
            cartItemRepository.delete(cartItem);
        });
    }

    // Cập nhật số lượng
    public void updateQuantity(User user, Integer productId, int quantity) {
        Cart cart = getCartByUser(user);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new RuntimeException("Item not in cart"));

        if (quantity <= 0) {
            removeFromCart(user, productId);
        } else {
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }
    }

    // Tính tổng tiền giỏ hàng
    public BigDecimal getTotal(Cart cart) {
        return cart.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}