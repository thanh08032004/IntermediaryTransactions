package hsf302.group3.intermediarytransactions.controller;

import hsf302.group3.intermediarytransactions.entity.DepositRequest;
import hsf302.group3.intermediarytransactions.entity.DepositStatus;
import hsf302.group3.intermediarytransactions.repository.DepositRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Controller
@RequiredArgsConstructor
public class DepositController {

    private final DepositRequestRepository depositRepository;

    @GetMapping("/deposit")
    public String showDepositPage() {
        return "home";
    }

    @PostMapping("/deposit")
    public String createQR(@RequestParam BigDecimal amount, Model model) {

        // Validate
        if (amount.compareTo(new BigDecimal("10000")) < 0) {
            model.addAttribute("error", "Tối thiểu 10,000 VND");
            return "home";
        }

        // Save DB
        DepositRequest req = DepositRequest.builder()
                .userId(2) // TODO: replace bằng user login
                .amount(amount)
                .status(DepositStatus.PENDING)
                .build();

        DepositRequest saved = depositRepository.save(req);

        // Nội dung chuyển khoản
        String content = "DEPOSIT" + saved.getId();

        // QR URL
        String qrUrl = "https://img.vietqr.io/image/MB-0326538343-compact.png"
                + "?amount=" + amount.setScale(0, RoundingMode.DOWN)
                + "&addInfo=" + content
                + "&accountName=INTERMEDIARY";

        // Data ra view
        model.addAttribute("qrUrl", qrUrl);
        model.addAttribute("amount", amount.setScale(0, RoundingMode.DOWN));
        model.addAttribute("content", content);

        return "payment";
    }
}