package hsf302.group3.intermediarytransactions.config;

import hsf302.group3.intermediarytransactions.entity.User;
import hsf302.group3.intermediarytransactions.entity.Wallet;
import hsf302.group3.intermediarytransactions.repository.UserRepository;
import hsf302.group3.intermediarytransactions.repository.WalletRepository;
import hsf302.group3.intermediarytransactions.service.NotificationService;
import hsf302.group3.intermediarytransactions.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.math.BigDecimal;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    @ModelAttribute
    public void addGlobalData(Model model){

        String username = SecurityUtil.getCurrentUsername();

        if (username == null) {
            model.addAttribute("balance", BigDecimal.ZERO);
            return;
        }

        User user = userRepository.findByUsername(username).orElse(null);
        if(user == null){
            model.addAttribute("balance", BigDecimal.ZERO);
            return;
        }

        model.addAttribute("notifications",
                notificationService.getNotificationsByUserId(user.getId()));

        model.addAttribute("unreadCount",
                notificationService.countUnread(user.getId()));

        BigDecimal balance = walletRepository.findByUserId(user.getId())
                .map(Wallet::getBalance)
                .orElse(BigDecimal.ZERO);

        model.addAttribute("balance", balance);
    }
}