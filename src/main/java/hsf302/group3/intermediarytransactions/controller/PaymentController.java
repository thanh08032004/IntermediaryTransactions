package hsf302.group3.intermediarytransactions.controller;

import hsf302.group3.intermediarytransactions.entity.Order;
import hsf302.group3.intermediarytransactions.entity.PaymentStatus;
import hsf302.group3.intermediarytransactions.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class PaymentController {

    private final OrderRepository orderRepository;

    @GetMapping("/payment/{orderId}")
    public String showPayment(@PathVariable Integer orderId, Model model) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // 🛑 nếu đã thanh toán thì không cho tạo QR nữa
        if (order.getPaymentStatus() == PaymentStatus.SUCCESS) {
            return "redirect:/success?orderId=" + orderId;
        }

        // 🔥 chỉ tạo paymentCode nếu chưa có
        if (order.getPaymentCode() == null) {
            String content = "ORDER_" + order.getId();
            order.setPaymentCode(content);
            order.setPaymentStatus(PaymentStatus.PENDING);
            orderRepository.save(order);
        }

        String content = order.getPaymentCode();

        // 🔥 convert amount cho chắc
        String amountStr = order.getTotalAmount().setScale(0, BigDecimal.ROUND_HALF_UP) // bỏ .00
                .toPlainString();

        String qrUrl = "https://img.vietqr.io/image/MB-0326538343-compact.png"
                + "?amount=" + amountStr
                + "&addInfo=" + content;

        model.addAttribute("amount", amountStr);
        model.addAttribute("content", content);
        model.addAttribute("qrUrl", qrUrl);
        model.addAttribute("orderId", order.getId());

        return "payment";
    }
    @GetMapping("/payment-request")
    public String showRequestPage() {
        return "payment_request"; // trang nhập tiền
    }
}