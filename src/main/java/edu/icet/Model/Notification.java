package edu.icet.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    private Integer id;
    private LocalDateTime dateTime;
    private String roomNumber;
    private String customerName;
    private String message;
    private NotificationStatus status;
    private Integer reservationId;
    
    public enum NotificationStatus {
        UNREAD,
        READ,
        ARCHIVED
    }
} 