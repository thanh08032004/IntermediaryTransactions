package hsf302.group3.intermediarytransactions.service;

import hsf302.group3.intermediarytransactions.entity.Order;
import hsf302.group3.intermediarytransactions.entity.PaymentStatus;
import hsf302.group3.intermediarytransactions.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;

    public Order createOrder(Integer userId, Double amount) {

        String paymentCode = "SEPAY_" + System.currentTimeMillis();

        Order order = Order.builder()
                .orderCode("ORD_" + System.currentTimeMillis())
                .userId(userId)
                .status("PENDING")
                .totalAmount(BigDecimal.valueOf(amount))
                .paymentCode(paymentCode)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        return orderRepository.save(order);
    }
}