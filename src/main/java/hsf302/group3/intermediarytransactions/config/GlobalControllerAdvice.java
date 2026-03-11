package hsf302.group3.intermediarytransactions.config;

import hsf302.group3.intermediarytransactions.entity.User;
import hsf302.group3.intermediarytransactions.repository.UserRepository;
import hsf302.group3.intermediarytransactions.service.NotificationService;
import hsf302.group3.intermediarytransactions.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @ModelAttribute
    public void addNotifications(Model model){

        String username = SecurityUtil.getCurrentUsername();

        // tìm user
        User user = userRepository.findByUsername(username).orElse(null);
        if(user == null) return;

        model.addAttribute("notifications",
                notificationService.getNotificationsByUserId(user.getId()));

        model.addAttribute("unreadCount",
                notificationService.countUnread(user.getId()));
    }
}
