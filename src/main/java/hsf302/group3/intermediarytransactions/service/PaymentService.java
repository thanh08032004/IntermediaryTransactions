package hsf302.group3.intermediarytransactions.service;

import hsf302.group3.intermediarytransactions.entity.Order;
import hsf302.group3.intermediarytransactions.entity.PaymentStatus;
import hsf302.group3.intermediarytransactions.entity.User;
import hsf302.group3.intermediarytransactions.repository.OrderRepository;
import hsf302.group3.intermediarytransactions.util.constant.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;

    public Order createOrder(Integer userId, Double amount) {

        String paymentCode = "SEPAY_" + System.currentTimeMillis();

        // ✅ tạo seller từ userId
        User seller = new User();
        seller.setId(userId);

        Order order = Order.builder()
                .orderCode("ORD_" + System.currentTimeMillis())
                .seller(seller)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(amount))
                .paymentCode(paymentCode)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        return orderRepository.save(order);
    }
}