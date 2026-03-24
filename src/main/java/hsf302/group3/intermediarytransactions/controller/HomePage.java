package hsf302.group3.intermediarytransactions.controller;

import hsf302.group3.intermediarytransactions.entity.User;
import hsf302.group3.intermediarytransactions.entity.Wallet;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;

import hsf302.group3.intermediarytransactions.entity.User;
import hsf302.group3.intermediarytransactions.entity.Wallet;
import hsf302.group3.intermediarytransactions.repository.UserRepository;
import hsf302.group3.intermediarytransactions.repository.WalletRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class HomePage {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    @GetMapping("/")
    public String showHomePage() {
        return "home";
    }

    @ModelAttribute("balance")
    public BigDecimal getBalance(Authentication auth) {
        if (auth == null) return BigDecimal.ZERO;

        String username = auth.getName();

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return BigDecimal.ZERO;

        return walletRepository.findByUserId(user.getId())
                .map(Wallet::getBalance)
                .orElse(BigDecimal.ZERO);
    }
}
