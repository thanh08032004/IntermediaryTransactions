package hsf302.group3.intermediarytransactions.controller;

import hsf302.group3.intermediarytransactions.entity.Notification;
import hsf302.group3.intermediarytransactions.entity.User;
import hsf302.group3.intermediarytransactions.service.NotificationService;
import hsf302.group3.intermediarytransactions.service.UserService;
import hsf302.group3.intermediarytransactions.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping("/notifications/read/{id}")
    public String readNotification(@PathVariable Integer id){

        notificationService.markAsRead(id);

        return "redirect:/";
    }

    @GetMapping("/notifications")
    public String viewAllNotifications(Model model){

        String username = SecurityUtil.getCurrentUsername();

        User user = userService.findByUsername(username);

        notificationService.markAllAsRead(user.getId());

        List<Notification> list =
                notificationService.getNotificationsByUserId(user.getId());

        model.addAttribute("notifications", list);

        return "redirect:/";
    }

}
