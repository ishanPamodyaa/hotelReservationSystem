package edu.icet.controller;

import edu.icet.Utill.Role;
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
import java.util.ResourceBundle;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set the dashboard as the active button initially
        setActiveButton(dashboardBtn);
        
        // Initialize dashboard data (hardcoded for now, would come from database in real app)
        totalRoomsLabel.setText("100");
        availableRoomsLabel.setText("75");
        occupiedRoomsLabel.setText("25");
        
        // Set revenue amount with dollar sign
        updateRevenueLabel(12500);
        
        // Add placeholder data
        populatePlaceholderData();
    }
    
    /**
     * Updates the revenue label with proper currency formatting
     * @param amount Amount in dollars
     */
    private void updateRevenueLabel(double amount) {
        String formattedAmount = String.format("$%,.0f", amount);
        todayRevenueLabel.setText(formattedAmount);
    }
    
    private void populatePlaceholderData() {

        for (int i = 1; i <= 3; i++) {
            Label label = new Label("Reservation #" + (1000 + i) + " - John Doe - Room " + (100 + i) + " - " + i + " day(s)");
            recentReservationsContainer.getChildren().add(label);
        }
        
        // Today's check-ins placeholder
        for (int i = 1; i <= 3; i++) {
            Label label = new Label("Check-in #" + (2000 + i) + " - Jane Smith - Room " + (200 + i));
            todayCheckInsContainer.getChildren().add(label);
        }
        
        // Today's check-outs placeholder
        for (int i = 1; i <= 3; i++) {
            Label label = new Label("Check-out #" + (3000 + i) + " - Bob Johnson - Room " + (300 + i));
            todayCheckOutsContainer.getChildren().add(label);
        }
        
        // Recent activities placeholder
        for (int i = 1; i <= 3; i++) {
            Label label = new Label("User Admin modified reservation #" + (4000 + i) + " - " + i + " hour(s) ago");
            recentActivitiesContainer.getChildren().add(label);
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
        // Show the main dashboard pane
        dashboardPane.setVisible(true);
        contentArea.getChildren().setAll(dashboardPane);
        contentTitle.setText("Dashboard Overview");
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
        loadContentPane("/view/Reports.fxml", "Report Generation");
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

    public void setLoggedInUser(Role user) {
        System.out.println("dhsagdagdhag");
    }
} 