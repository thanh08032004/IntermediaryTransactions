package hsf302.group3.intermediarytransactions.controller;

import hsf302.group3.intermediarytransactions.entity.*;
import hsf302.group3.intermediarytransactions.repository.DepositRequestRepository;
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
    private final DepositRequestRepository depositRequestRepository;

    @PostMapping("/sepay-webhook")
    public String handleWebhook(@RequestBody Map<String, Object> payload) {

        System.out.println("🔥 WEBHOOK: " + payload);

        String content = (String) payload.get("content");
        Object amountObj = payload.get("transferAmount");

        if (content == null || amountObj == null) {
            return "INVALID DATA";
        }

        BigDecimal amount = new BigDecimal(amountObj.toString());

        if (content != null && content.contains("DEPOSIT")) {

            String depositPart = content.substring(content.indexOf("DEPOSIT") + 7);

            String idStr = depositPart.split("[^0-9]")[0];

            Long id = Long.parseLong(idStr);

            System.out.println("DEPOSIT ID = " + id);

            DepositRequest req = depositRequestRepository
                    .findById(Math.toIntExact(id))
                    .orElse(null);

            if (req == null) return "NOT FOUND";

            if (req.getStatus() == DepositStatus.SUCCESS) {
                return "ALREADY";
            }

            if (amount.compareTo(req.getAmount()) < 0) {
                return "INVALID AMOUNT";
            }

            req.setStatus(DepositStatus.SUCCESS);
            depositRequestRepository.save(req);

            System.out.println("CALL DEPOSIT SERVICE");

            walletService.deposit(
                    req.getUserId(),
                    req.getAmount(),
                    "Nap tien - " + content
            );

            return "OK DEPOSIT";
        }
        if (content.startsWith("ORDER_")) {

            Order order = orderRepository.findByPaymentCode(content).orElse(null);

            if (order == null) return "NOT FOUND";

            if (order.getPaymentStatus() == PaymentStatus.SUCCESS) {
                return "ALREADY PAID";
            }

            if (amount.compareTo(order.getTotalAmount()) < 0) {
                return "INVALID AMOUNT";
            }

            order.setPaymentStatus(PaymentStatus.SUCCESS);
            order.setStatus("COMPLETED");
            orderRepository.save(order);

            walletService.deposit(
                    order.getUserId(),
                    order.getTotalAmount(),
                    "Thanh toán - " + content
            );

            return "OK ORDER";
        }

        return "UNKNOWN TYPE";
    }
}