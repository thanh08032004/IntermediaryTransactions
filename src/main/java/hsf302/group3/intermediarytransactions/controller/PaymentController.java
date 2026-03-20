package hsf302.group3.intermediarytransactions.controller;

import hsf302.group3.intermediarytransactions.entity.Order;
import hsf302.group3.intermediarytransactions.entity.PaymentStatus;
import hsf302.group3.intermediarytransactions.entity.User;
import hsf302.group3.intermediarytransactions.entity.Wallet;
import hsf302.group3.intermediarytransactions.repository.OrderRepository;
import hsf302.group3.intermediarytransactions.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class PaymentController {

    private final OrderRepository orderRepository;
    private final WalletRepository walletRepository;
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
    @PostMapping("/payment/wallet/{orderId}")
    public String payWithWallet(@PathVariable Integer orderId,
                                RedirectAttributes redirectAttributes) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getPaymentStatus() == PaymentStatus.SUCCESS) {
            return "redirect:/success?orderId=" + orderId;
        }

        User buyer = order.getBuyer();
        if (buyer == null) {
            throw new RuntimeException("Order chưa có buyer!");
        }

        Wallet wallet = buyer.getWallet();
        if (wallet == null) {
            throw new RuntimeException("User chưa có ví!");
        }

        BigDecimal balance = wallet.getBalance() == null
                ? BigDecimal.ZERO
                : wallet.getBalance();

        BigDecimal total = order.getTotalAmount();

        if (balance.compareTo(total) < 0) {
            redirectAttributes.addFlashAttribute("error", "Tài khoản không đủ tiền!");
            return "redirect:/wallet/deposit";
        }

        wallet.setBalance(balance.subtract(total));

        order.setPaymentStatus(PaymentStatus.SUCCESS);

        walletRepository.save(wallet);
        orderRepository.save(order);

        return "redirect:/success?orderId=" + orderId;
    }
    @GetMapping("/payment-request")
    public String showRequestPage() {
        return "payment_request"; // trang nhập tiền
    }
}