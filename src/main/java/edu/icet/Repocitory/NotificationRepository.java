package edu.icet.Repocitory;

import edu.icet.Model.Notification;
import edu.icet.Model.Notification.NotificationStatus;
import java.time.LocalDate;
import java.util.List;

public interface NotificationRepository {
    List<Notification> findAll();
    List<Notification> findByStatus(NotificationStatus status);
    List<Notification> findByDate(LocalDate date);
    List<Notification> findByStatusAndDate(NotificationStatus status, LocalDate date);
    void markAsRead(Integer notificationId);
    void markAllAsRead();
    void save(Notification notification);
} 