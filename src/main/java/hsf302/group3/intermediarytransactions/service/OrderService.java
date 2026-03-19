package hsf302.group3.intermediarytransactions.service;

import hsf302.group3.intermediarytransactions.entity.*;
import hsf302.group3.intermediarytransactions.repository.OrderRepository;
import hsf302.group3.intermediarytransactions.util.constant.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;

    public Order createOrderFromCart(User user) {

        Cart cart = cartService.getCartByUser(user);

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Order order = Order.builder()
                .buyer(user)
                .orderCode("ORD-" + UUID.randomUUID().toString().substring(0,8))
                .status(OrderStatus.PENDING)
                .build();

        // Convert CartItem -> OrderItem
        for (CartItem cartItem : cart.getItems()) {
            OrderItem item = OrderItem.builder()
                    .product(cartItem.getProduct())
                    .price(cartItem.getPrice())
                    .quantity(cartItem.getQuantity())
                    .build();

            item.calculateSubtotal();
            order.addItem(item);
        }

        // tính tổng
        order.calculateTotal();


        if (!cart.getItems().isEmpty()) {
            order.setSeller(cart.getItems().get(0).getProduct().getSupplier());
        }

        Order savedOrder = orderRepository.save(order);

        // clear cart
        cart.getItems().clear();

        return savedOrder;
    }
}
