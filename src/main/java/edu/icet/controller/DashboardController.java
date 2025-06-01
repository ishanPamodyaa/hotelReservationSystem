package edu.icet.controller;

import edu.icet.Utill.Role;
import edu.icet.Model.Notification;
import edu.icet.Repocitory.NotificationRepository;
import edu.icet.Repocitory.impl.NotificationRepositoryImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.List;
import edu.icet.Db.DBConnection;

public class DashboardController implements Initializable {

    @FXML
    private Button activityLogBtn;

    @FXML
    private Label availableRoomsLabel;

    @FXML
    private Button billingBtn;

    @FXML
    private Button checkInOutBtn;

    @FXML
    private StackPane contentArea;

    @FXML
    private Label contentTitle;

    @FXML
    private Button customerBtn;

    @FXML
    private Button dashboardBtn;

    @FXML
    private AnchorPane dashboardPane;

    @FXML
    private Button notificationBtn;

    @FXML
    private Label occupiedRoomsLabel;

    @FXML
    private VBox recentActivitiesContainer;

    @FXML
    private VBox recentReservationsContainer;

    @FXML
    private Button reportBtn;

    @FXML
    private Button reservationBtn;

    @FXML
    private Button roomManagementBtn;

    @FXML
    private Label todayRevenueLabel;

    @FXML
    private VBox todayCheckInsContainer;

    @FXML
    private VBox todayCheckOutsContainer;

    @FXML
    private Label totalRoomsLabel;

    @FXML
    private Button userManagementBtn;

    @FXML
    private Label userNameLabel;

    private final NotificationRepository notificationRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public DashboardController() {
        this.notificationRepository = new NotificationRepositoryImpl();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setActiveButton(dashboardBtn);
        loadDashboardData();
        loadRecentActivities();
    }
    
    private void loadDashboardData() {
        try (Connection conn = DBConnection.getInstance().getConnection()) {
            // Load room statistics
            loadRoomStatistics(conn);
            
            // Load today's revenue
            loadTodayRevenue(conn);
            
            // Load recent reservations
            loadRecentReservations(conn);
            
            // Load today's check-ins and check-outs
            loadTodayCheckInOuts(conn);
            
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorLabels();
        }
    }
    
    private void loadRoomStatistics(Connection conn) throws SQLException {
        String sql = "SELECT " +
                    "(SELECT COUNT(*) FROM rooms) as total_rooms, " +
                    "(SELECT COUNT(*) FROM rooms WHERE status = 'AVAILABLE') as available_rooms, " +
                    "(SELECT COUNT(*) FROM rooms WHERE status = 'OCCUPIED') as occupied_rooms";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                totalRoomsLabel.setText(String.valueOf(rs.getInt("total_rooms")));
                availableRoomsLabel.setText(String.valueOf(rs.getInt("available_rooms")));
                occupiedRoomsLabel.setText(String.valueOf(rs.getInt("occupied_rooms")));
            }
        }
    }
    
    private void loadTodayRevenue(Connection conn) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) as total_revenue " +
                    "FROM payments WHERE DATE(payment_date) = CURRENT_DATE";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                updateRevenueLabel(rs.getDouble("total_revenue"));
            }
        }
    }
    
    private void loadRecentReservations(Connection conn) throws SQLException {
        String sql = "SELECT r.id, r.room_number, c.name, r.duration_days " +
                    "FROM reservations r " +
                    "JOIN customers c ON r.customer_id = c.id " +
                    "ORDER BY r.created_at DESC LIMIT 3";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            recentReservationsContainer.getChildren().clear();
            while (rs.next()) {
                String text = String.format("Reservation #%d - %s - Room %s - %d day(s)",
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("room_number"),
                    rs.getInt("duration_days"));
                Label label = createStyledLabel(text);
                recentReservationsContainer.getChildren().add(label);
            }
        }
    }
    
    private void loadTodayCheckInOuts(Connection conn) throws SQLException {
        // Load Check-ins
        String checkInSql = "SELECT r.id, c.name, r.room_number " +
                          "FROM reservations r " +
                          "JOIN customers c ON r.customer_id = c.id " +
                          "WHERE DATE(check_in_date) = CURRENT_DATE " +
                          "LIMIT 3";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkInSql)) {
            
            todayCheckInsContainer.getChildren().clear();
            while (rs.next()) {
                String text = String.format("Check-in #%d - %s - Room %s",
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("room_number"));
                Label label = createStyledLabel(text);
                todayCheckInsContainer.getChildren().add(label);
            }
        }
        
        // Load Check-outs
        String checkOutSql = "SELECT r.id, c.name, r.room_number " +
                           "FROM reservations r " +
                           "JOIN customers c ON r.customer_id = c.id " +
                           "WHERE DATE(check_out_date) = CURRENT_DATE " +
                           "LIMIT 3";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkOutSql)) {
            
            todayCheckOutsContainer.getChildren().clear();
            while (rs.next()) {
                String text = String.format("Check-out #%d - %s - Room %s",
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("room_number"));
                Label label = createStyledLabel(text);
                todayCheckOutsContainer.getChildren().add(label);
            }
        }
    }
    
    private void loadRecentActivities() {
        List<Notification> recentNotifications = notificationRepository.findAll();
        recentActivitiesContainer.getChildren().clear();
        
        recentNotifications.stream()
            .limit(3)
            .forEach(notification -> {
                String text = String.format("%s - %s",
                    notification.getMessage(),
                    notification.getDateTime().format(formatter));
                Label label = createStyledLabel(text);
                recentActivitiesContainer.getChildren().add(label);
            });
    }
    
    private Label createStyledLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-padding: 5 10; -fx-background-color: #f8f9fa; -fx-background-radius: 5; " +
                      "-fx-border-color: #dee2e6; -fx-border-radius: 5;");
        return label;
    }

    private void showErrorLabels() {
        totalRoomsLabel.setText("Error");
        availableRoomsLabel.setText("Error");
        occupiedRoomsLabel.setText("Error");
        todayRevenueLabel.setText("Error");
    }

    private void updateRevenueLabel(double amount) {
        String formattedAmount = String.format("$%,.2f", amount);
        todayRevenueLabel.setText(formattedAmount);
    }

    // Set active button style
    private void setActiveButton(Button button) {
        // Reset all buttons to transparent background
        dashboardBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        roomManagementBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        reservationBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        checkInOutBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        customerBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        billingBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        reportBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        notificationBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        userManagementBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        activityLogBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        
        // Set the active button with a different background color
        button.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;");
    }

    // Event handlers for sidebar buttons
    @FXML
    void handleDashboardAction(ActionEvent event) {
        setActiveButton(dashboardBtn);
        dashboardPane.setVisible(true);
        contentArea.getChildren().setAll(dashboardPane);
        contentTitle.setText("Dashboard Overview");
        loadDashboardData(); // Refresh data when returning to dashboard
    }

    @FXML
    void handleRoomManagementAction(ActionEvent event) {
        setActiveButton(roomManagementBtn);
        loadContentPane("/view/RoomManagement.fxml", "Room Management");
    }

    @FXML
    void handleReservationAction(ActionEvent event) {
        setActiveButton(reservationBtn);
        loadContentPane("/view/ReservationManagement.fxml", "Reservation Management");
    }

    @FXML
    void handleCheckInOutAction(ActionEvent event) {
        setActiveButton(checkInOutBtn);
        loadContentPane("/view/CheckInOut.fxml", "Check-in / Check-out");
    }

    @FXML
    void handleCustomerAction(ActionEvent event) {
        setActiveButton(customerBtn);
        loadContentPane("/view/CustomerManagement.fxml", "Customer Management");
    }

    @FXML
    void handleBillingAction(ActionEvent event) {
        setActiveButton(billingBtn);
        loadContentPane("/view/BillingManagement.fxml", "Billing / Invoicing");
    }

    @FXML
    void handleReportAction(ActionEvent event) {
        setActiveButton(reportBtn);
        loadContentPane("/view/InvoiceReports.fxml", "Invoice Reports");
    }

    @FXML
    void handleNotificationAction(ActionEvent event) {
        setActiveButton(notificationBtn);
        loadContentPane("/view/Notifications.fxml", "Notifications");
    }

    @FXML
    void handleUserManagementAction(ActionEvent event) {
        setActiveButton(userManagementBtn);
        loadContentPane("/view/UserManagement.fxml", "User Management");
    }

    @FXML
    void handleActivityLogAction(ActionEvent event) {
        setActiveButton(activityLogBtn);
        loadContentPane("/view/ActivityLogs.fxml", "Activity Logs");
    }

    @FXML
    void handleLogoutAction(ActionEvent event) {
        try {
            // Return to login screen
            Parent root = FXMLLoader.load(getClass().getResource("/view/LoginForm.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Hotel Reservation System - Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadContentPane(String fxmlPath, String title) {
        try {
            contentTitle.setText(title);
            
            try {
                URL fxmlUrl = getClass().getResource(fxmlPath);
                if (fxmlUrl != null) {
                    Parent node = FXMLLoader.load(fxmlUrl);
                    contentArea.getChildren().setAll(node);
                } else {
                    // Create a placeholder pane
                    AnchorPane placeholder = new AnchorPane();
                    placeholder.setStyle("-fx-background-color: white;");
                    
                    Label placeholderLabel = new Label(title + " Module - Coming Soon");
                    placeholderLabel.setStyle("-fx-font-size: 24px;");
                    placeholderLabel.setLayoutX(20);
                    placeholderLabel.setLayoutY(20);
                    
                    placeholder.getChildren().add(placeholderLabel);
                    contentArea.getChildren().setAll(placeholder);
                }
            } catch (IOException e) {
                e.printStackTrace();
                // Show error placeholder
                AnchorPane errorPane = new AnchorPane();
                errorPane.setStyle("-fx-background-color: white;");
                
                Label errorLabel = new Label("Error loading " + title + ": " + e.getMessage());
                errorLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: red;");
                errorLabel.setLayoutX(20);
                errorLabel.setLayoutY(20);
                
                errorPane.getChildren().add(errorLabel);
                contentArea.getChildren().setAll(errorPane);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setLoggedInUser(Role user) {
        if (user != null) {
            userNameLabel.setText("Welcome, " + user);
        }
    }
} 