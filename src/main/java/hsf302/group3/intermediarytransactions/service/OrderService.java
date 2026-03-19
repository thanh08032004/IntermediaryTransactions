package hsf302.group3.intermediarytransactions.service;

import hsf302.group3.intermediarytransactions.entity.*;
import hsf302.group3.intermediarytransactions.repository.*;
import hsf302.group3.intermediarytransactions.util.constant.OrderStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository; // thêm để lấy managed user

    @Transactional
    public Order createOrderFromCart(User user) {
        // 1. Lấy managed user từ DB
        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        // 2. Lấy cart
        Cart cart = cartRepository.findByUser(managedUser).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(managedUser);
            return cartRepository.save(newCart);
        });

        if (cart == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // 3. Tạo order
        Order order = Order.builder()
                .buyer(managedUser) // dùng managed user
                .orderCode("ORD-" + UUID.randomUUID().toString().substring(0, 8))
                .status(OrderStatus.PENDING)
                .build();

        // Lấy seller từ sản phẩm đầu tiên (nếu muốn)
        order.setSeller(cart.getItems().get(0).getProduct().getSupplier());

        // 4. Save order trước để có ID
        Order savedOrder = orderRepository.save(order);

        // 5. Chuyển CartItem → OrderItem
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .order(savedOrder)
                    .product(cartItem.getProduct())
                    .price(cartItem.getPrice())
                    .quantity(cartItem.getQuantity())
                    .build();
            orderItem.calculateSubtotal();
            orderItemRepository.save(orderItem);

            savedOrder.addItem(orderItem); // thêm vào order.items
        }

        // 6. Tính tổng
        savedOrder.calculateTotal();
        orderRepository.save(savedOrder); // update tổng

        // 7. Xóa cart items
        cart.getItems().clear();
        cartRepository.save(cart);

        return savedOrder;
    }
}