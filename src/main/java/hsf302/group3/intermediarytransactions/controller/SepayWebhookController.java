package hsf302.group3.intermediarytransactions.controller;

import hsf302.group3.intermediarytransactions.entity.*;
import hsf302.group3.intermediarytransactions.repository.OrderRepository;
import hsf302.group3.intermediarytransactions.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SepayWebhookController {

    private final OrderRepository orderRepository;
    private final WalletService walletService;

    @PostMapping("/sepay-webhook")
    public String handleWebhook(@RequestBody Map<String, Object> payload) {

        System.out.println("🔥 WEBHOOK RECEIVED: " + payload);

        String content = (String) payload.get("content");
        Number amountRaw = (Number) payload.get("transferAmount");

        if (content == null) return "NO CONTENT";

        // 🔍 tìm order
        Order order = orderRepository.findByPaymentCode(content).orElse(null);

        if (order == null) {
            return "NOT FOUND";
        }

        // 🛑 tránh cộng tiền 2 lần
        if (order.getPaymentStatus() == PaymentStatus.SUCCESS) {
            return "ALREADY PAID";
        }

        // 💰 convert tiền
        BigDecimal amount = amountRaw != null
                ? BigDecimal.valueOf(amountRaw.doubleValue())
                : BigDecimal.ZERO;

        // ❌ check thiếu tiền
        if (amount.compareTo(order.getTotalAmount()) < 0) {
            return "INVALID AMOUNT";
        }

        // =========================
        // ✅ UPDATE ORDER
        // =========================
        order.setPaymentStatus(PaymentStatus.SUCCESS);
        order.setStatus("COMPLETED");
        orderRepository.save(order);

        // =========================
        // 💰 CỘNG TIỀN VÀO VÍ
        // =========================
        walletService.deposit(
                order.getUserId(),
                order.getTotalAmount(),
                "Nap tien tu QR - " + order.getPaymentCode()
        );

        System.out.println("💰 Thanh toán thành công order: " + order.getId());

        return "OK";
    }
}