package edu.icet.Repocitory.impl;

import edu.icet.Model.Notification;
import edu.icet.Model.Notification.NotificationStatus;
import edu.icet.Repocitory.NotificationRepository;
import edu.icet.Db.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationRepositoryImpl implements NotificationRepository {
    
    @Override
    public List<Notification> findAll() {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications ORDER BY date_time DESC";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                notifications.add(mapResultSetToNotification(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return notifications;
    }
    
    @Override
    public List<Notification> findByStatus(NotificationStatus status) {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE status = ? ORDER BY date_time DESC";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    notifications.add(mapResultSetToNotification(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return notifications;
    }
    
    @Override
    public List<Notification> findByDate(LocalDate date) {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE DATE(date_time) = ? ORDER BY date_time DESC";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(date));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    notifications.add(mapResultSetToNotification(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return notifications;
    }
    
    @Override
    public List<Notification> findByStatusAndDate(NotificationStatus status, LocalDate date) {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE status = ? AND DATE(date_time) = ? ORDER BY date_time DESC";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status.name());
            stmt.setDate(2, Date.valueOf(date));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    notifications.add(mapResultSetToNotification(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return notifications;
    }
    
    @Override
    public void markAsRead(Integer notificationId) {
        String sql = "UPDATE notifications SET status = ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, NotificationStatus.READ.name());
            stmt.setInt(2, notificationId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void markAllAsRead() {
        String sql = "UPDATE notifications SET status = ? WHERE status = ?";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, NotificationStatus.READ.name());
            stmt.setString(2, NotificationStatus.UNREAD.name());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void save(Notification notification) {
        String sql = "INSERT INTO notifications (date_time, room_number, customer_name, message, status, reservation_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(notification.getDateTime()));
            stmt.setString(2, notification.getRoomNumber());
            stmt.setString(3, notification.getCustomerName());
            stmt.setString(4, notification.getMessage());
            stmt.setString(5, notification.getStatus().name());
            stmt.setObject(6, notification.getReservationId());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        return new Notification(
            rs.getInt("id"),
            rs.getTimestamp("date_time").toLocalDateTime(),
            rs.getString("room_number"),
            rs.getString("customer_name"),
            rs.getString("message"),
            NotificationStatus.valueOf(rs.getString("status")),
            rs.getInt("reservation_id")
        );
    }
} 