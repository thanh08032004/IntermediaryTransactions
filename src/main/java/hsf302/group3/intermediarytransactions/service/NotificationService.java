package hsf302.group3.intermediarytransactions.service;

import hsf302.group3.intermediarytransactions.entity.Notification;
import hsf302.group3.intermediarytransactions.entity.User;
import hsf302.group3.intermediarytransactions.repository.NotificationRepository;
import hsf302.group3.intermediarytransactions.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void send(Integer userId, String title, String message) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);

        notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsByUserId(Integer userId){
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public Long countUnread(Integer userId){
        return notificationRepository
                .countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Integer id){

        Notification noti = notificationRepository.findById(id).orElseThrow();

        noti.setIsRead(true);

        notificationRepository.save(noti);
    }

    @Transactional
    public void markAllAsRead(Integer userId){

        List<Notification> list =
                notificationRepository.findByUserId(userId);

        list.forEach(n -> n.setIsRead(true));
    }

}
