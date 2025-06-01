package edu.icet.controller;

import edu.icet.Model.Notification;
import edu.icet.Model.Notification.NotificationStatus;
import edu.icet.Repocitory.NotificationRepository;
import edu.icet.Repocitory.impl.NotificationRepositoryImpl;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class NotificationsController implements Initializable {
    @FXML private ComboBox<NotificationStatus> statusFilter;
    @FXML private DatePicker dateFilter;
    @FXML private TableView<Notification> notificationTable;
    @FXML private TableColumn<Notification, String> dateTimeColumn;
    @FXML private TableColumn<Notification, String> roomColumn;
    @FXML private TableColumn<Notification, String> customerColumn;
    @FXML private TableColumn<Notification, String> messageColumn;
    @FXML private TableColumn<Notification, NotificationStatus> statusColumn;
    @FXML private TableColumn<Notification, Void> actionsColumn;
    @FXML private Label totalNotificationsLabel;
    @FXML private Label unreadNotificationsLabel;

    private final NotificationRepository notificationRepository;
    private final ObservableList<Notification> notifications;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public NotificationsController() {
        this.notificationRepository = new NotificationRepositoryImpl();
        this.notifications = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupComboBox();
        setupTable();
        loadNotifications();
    }

    private void setupComboBox() {
        statusFilter.setItems(FXCollections.observableArrayList(NotificationStatus.values()));
    }

    private void setupTable() {
        dateTimeColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getDateTime().format(formatter)));
        roomColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        messageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        actionsColumn.setCellFactory(param -> new TableCell<Notification, Void>() {
            private final Button viewBtn = new Button("View");
            private final Button markReadBtn = new Button("Mark Read");
            private final HBox buttons = new HBox(5, viewBtn, markReadBtn);
            {
                viewBtn.setStyle("-fx-background-color: #0dcaf0; -fx-text-fill: white;");
                markReadBtn.setStyle("-fx-background-color: #198754; -fx-text-fill: white;");
                
                viewBtn.setOnAction(event -> {
                    Notification notification = getTableView().getItems().get(getIndex());
                    handleViewNotification(notification);
                });
                
                markReadBtn.setOnAction(event -> {
                    Notification notification = getTableView().getItems().get(getIndex());
                    handleMarkAsRead(notification);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
        
        notificationTable.setItems(notifications);
    }

    @FXML
    private void handleRefresh() {
        loadNotifications();
    }

    @FXML
    private void handleMarkAllRead() {
        notificationRepository.markAllAsRead();
        loadNotifications();
        showAlert("Success", "All notifications marked as read.", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleApplyFilters() {
        NotificationStatus status = statusFilter.getValue();
        LocalDate date = dateFilter.getValue();
        
        List<Notification> filteredNotifications;
        if (status != null && date != null) {
            filteredNotifications = notificationRepository.findByStatusAndDate(status, date);
        } else if (status != null) {
            filteredNotifications = notificationRepository.findByStatus(status);
        } else if (date != null) {
            filteredNotifications = notificationRepository.findByDate(date);
        } else {
            loadNotifications();
            return;
        }
        
        notifications.setAll(filteredNotifications);
        updateSummary();
    }

    @FXML
    private void handleResetFilters() {
        statusFilter.setValue(null);
        dateFilter.setValue(null);
        loadNotifications();
    }

    private void handleViewNotification(Notification notification) {
        // Navigate to the relevant reservation or checkout details
        if (notification.getReservationId() != null) {
            // TODO: Implement navigation to reservation details
            showAlert("Info", "Viewing details for reservation #" + notification.getReservationId(), 
                Alert.AlertType.INFORMATION);
        }
    }

    private void handleMarkAsRead(Notification notification) {
        notificationRepository.markAsRead(notification.getId());
        loadNotifications();
    }

    private void loadNotifications() {
        List<Notification> allNotifications = notificationRepository.findAll();
        notifications.setAll(allNotifications);
        updateSummary();
    }

    private void updateSummary() {
        totalNotificationsLabel.setText("Total Notifications: " + notifications.size());
        long unreadCount = notifications.stream()
            .filter(n -> n.getStatus() == NotificationStatus.UNREAD)
            .count();
        unreadNotificationsLabel.setText("Unread: " + unreadCount);
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 