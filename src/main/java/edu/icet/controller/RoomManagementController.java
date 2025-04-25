package edu.icet.controller;

import edu.icet.Db.DBConnection;
import edu.icet.Model.Room;
import edu.icet.Utill.RoomStatus;
import edu.icet.Utill.RoomType;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class RoomManagementController implements Initializable {

    @FXML
    private TextField roomNumberField;

    @FXML
    private ComboBox<RoomType> roomTypeComboBox;

    @FXML
    private TextField priceField;

    @FXML
    private ComboBox<RoomStatus> statusComboBox;

    @FXML
    private Button saveButton;

    @FXML
    private Button clearButton;

    @FXML
    private Label messageLabel;

    @FXML
    private TableView<Room> roomTable;

    @FXML
    private TableColumn<Room, Integer> idColumn;

    @FXML
    private TableColumn<Room, String> roomNumberColumn;

    @FXML
    private TableColumn<Room, String> typeColumn;

    @FXML
    private TableColumn<Room, Double> priceColumn;

    @FXML
    private TableColumn<Room, String> statusColumn;

    @FXML
    private TableColumn<Room, Void> actionColumn;

    @FXML
    private ComboBox<String> filterStatusComboBox;

    private ObservableList<Room> roomList = FXCollections.observableArrayList();
    private Room selectedRoom = null; // For editing

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set up the combo boxes
        roomTypeComboBox.getItems().addAll(RoomType.values());
        statusComboBox.getItems().addAll(RoomStatus.values());
        
        // Set up filter combo box
        filterStatusComboBox.getItems().add("All Rooms");
        for (RoomStatus status : RoomStatus.values()) {
            filterStatusComboBox.getItems().add(status.toString());
        }
        filterStatusComboBox.setValue("All Rooms");
        
        // Set up table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        roomNumberColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType().toString()));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().toString()));
        
        // Add action column (Edit/Delete buttons)
        addButtonsToTable();
        
        // Load rooms from database
        loadRooms();
    }

    private void addButtonsToTable() {
        Callback<TableColumn<Room, Void>, TableCell<Room, Void>> cellFactory = new Callback<TableColumn<Room, Void>, TableCell<Room, Void>>() {
            @Override
            public TableCell<Room, Void> call(final TableColumn<Room, Void> param) {
                return new TableCell<Room, Void>() {
                    private final Button editButton = new Button("Edit");
                    private final Button deleteButton = new Button("Delete");
                    private final HBox pane = new HBox(2);
                    
                    {
                        editButton.setStyle("-fx-background-color: #ffc107; -fx-text-fill: white; -fx-font-size: 10px;");
                        deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 10px;");
                        
                        editButton.setOnAction((ActionEvent event) -> {
                            Room room = getTableView().getItems().get(getIndex());
                            handleEditRoom(room);
                        });
                        
                        deleteButton.setOnAction((ActionEvent event) -> {
                            Room room = getTableView().getItems().get(getIndex());
                            handleDeleteRoom(room);
                        });
                        
                        pane.getChildren().addAll(editButton, deleteButton);
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(pane);
                        }
                    }
                };
            }
        };
        
        actionColumn.setCellFactory(cellFactory);
    }
    
    private void handleEditRoom(Room room) {
        // Populate form fields with room data
        roomNumberField.setText(room.getRoomNumber());
        roomTypeComboBox.setValue(room.getType());
        priceField.setText(String.valueOf(room.getPrice()));
        statusComboBox.setValue(room.getStatus());
        
        // Set selected room for later update
        selectedRoom = room;
        saveButton.setText("Update");
    }
    
    private void handleDeleteRoom(Room room) {
        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Room");
        alert.setHeaderText("Delete Room: " + room.getRoomNumber());
        alert.setContentText("Are you sure you want to delete this room?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteRoom(room);
            }
        });
    }
    
    private void deleteRoom(Room room) {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            if (connection == null) {
                showMessage("Unable to establish database connection", true);
                return;
            }
            
            String sql = "DELETE FROM rooms WHERE room_id = ?";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, room.getId());
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                showMessage("Room deleted successfully", false);
                loadRooms(); // Refresh table
            } else {
                showMessage("Failed to delete room", true);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showMessage("Database error: " + e.getMessage(), true);
        } finally {
            try {
                if (statement != null) statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void handleSave(ActionEvent event) {
        // Get form values
        String roomNumber = roomNumberField.getText().trim();
        RoomType type = roomTypeComboBox.getValue();
        String priceText = priceField.getText().trim();
        RoomStatus status = statusComboBox.getValue();
        
        // Validate inputs
        if (roomNumber.isEmpty()) {
            showMessage("Room number cannot be empty", true);
            return;
        }
        
        if (type == null) {
            showMessage("Please select a room type", true);
            return;
        }
        
        if (priceText.isEmpty()) {
            showMessage("Price cannot be empty", true);
            return;
        }
        
        double price;
        try {
            price = Double.parseDouble(priceText);
            if (price <= 0) {
                showMessage("Price must be greater than zero", true);
                return;
            }
        } catch (NumberFormatException e) {
            showMessage("Price must be a valid number", true);
            return;
        }
        
        if (status == null) {
            showMessage("Please select a status", true);
            return;
        }
        
        // If editing existing room
        if (selectedRoom != null) {
            updateRoom(selectedRoom.getId(), roomNumber, type, price, status);
        } else {
            // Check if room number already exists
            if (isRoomNumberExists(roomNumber)) {
                showMessage("Room number already exists", true);
                return;
            }
            
            // Add new room
            addRoom(roomNumber, type, price, status);
        }
    }
    
    private boolean isRoomNumberExists(String roomNumber) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            if (connection == null) return false;
            
            String sql = "SELECT COUNT(*) FROM rooms WHERE room_number = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, roomNumber);
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return false;
    }
    
    private void addRoom(String roomNumber, RoomType type, double price, RoomStatus status) {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            if (connection == null) {
                showMessage("Unable to establish database connection", true);
                return;
            }
            
            String sql = "INSERT INTO rooms (room_number, room_type, price_per_night, availability_status) VALUES (?, ?, ?, ?)";
            statement = connection.prepareStatement(sql);
            statement.setString(1, roomNumber);
            statement.setString(2, type.toString());
            statement.setDouble(3, price);
            statement.setString(4, status.toString());
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                showMessage("Room added successfully", false);
                clearFields();
                loadRooms(); // Refresh table
            } else {
                showMessage("Failed to add room", true);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showMessage("Database error: " + e.getMessage(), true);
        } finally {
            try {
                if (statement != null) statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void updateRoom(int roomId, String roomNumber, RoomType type, double price, RoomStatus status) {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            if (connection == null) {
                showMessage("Unable to establish database connection", true);
                return;
            }
            
            String sql = "UPDATE rooms SET room_number = ?, room_type = ?, price_per_night = ?, availability_status = ? WHERE room_id = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, roomNumber);
            statement.setString(2, type.toString());
            statement.setDouble(3, price);
            statement.setString(4, status.toString());
            statement.setInt(5, roomId);
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                showMessage("Room updated successfully", false);
                clearFields();
                loadRooms(); // Refresh table
            } else {
                showMessage("Failed to update room", true);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showMessage("Database error: " + e.getMessage(), true);
        } finally {
            try {
                if (statement != null) statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void handleClear(ActionEvent event) {
        clearFields();
    }
    
    private void clearFields() {
        roomNumberField.clear();
        roomTypeComboBox.setValue(null);
        priceField.clear();
        statusComboBox.setValue(null);
        messageLabel.setText("");
        
        // Reset selected room
        selectedRoom = null;
        saveButton.setText("Save");
    }
    
    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.setTextFill(isError ? javafx.scene.paint.Color.RED : javafx.scene.paint.Color.GREEN);
    }

    @FXML
    void handleRefresh(ActionEvent event) {
        loadRooms();
    }
    
    @FXML
    void handleFilter(ActionEvent event) {
        String filter = filterStatusComboBox.getValue();
        if (filter == null || filter.equals("All Rooms")) {
            loadRooms();
        } else {
            loadRoomsByStatus(RoomStatus.valueOf(filter));
        }
    }
    
    private void loadRoomsByStatus(RoomStatus status) {
        roomList.clear();
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            if (connection == null) {
                showMessage("Unable to establish database connection", true);
                return;
            }
            
            String sql = "SELECT * FROM rooms WHERE availability_status = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, status.toString());
            resultSet = statement.executeQuery();
            
            while (resultSet.next()) {
                int id = resultSet.getInt("room_id");
                String roomNumber = resultSet.getString("room_number");
                RoomType type = RoomType.valueOf(resultSet.getString("room_type"));
                double price = resultSet.getDouble("price_per_night");
                RoomStatus roomStatus = RoomStatus.valueOf(resultSet.getString("availability_status"));
                
                Room room = new Room(id, roomNumber, type, price, roomStatus);
                roomList.add(room);
            }
            
            roomTable.setItems(roomList);
            
        } catch (SQLException e) {
            e.printStackTrace();
            showMessage("Error loading rooms: " + e.getMessage(), true);
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void loadRooms() {
        roomList.clear();
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            if (connection == null) {
                showMessage("Unable to establish database connection", true);
                return;
            }
            
            String sql = "SELECT * FROM rooms";
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            
            while (resultSet.next()) {
                int id = resultSet.getInt("room_id");
                String roomNumber = resultSet.getString("room_number");
                RoomType type = RoomType.valueOf(resultSet.getString("room_type"));
                double price = resultSet.getDouble("price_per_night");
                RoomStatus status = RoomStatus.valueOf(resultSet.getString("availability_status"));
                
                Room room = new Room(id, roomNumber, type, price, status);
                roomList.add(room);
            }
            
            roomTable.setItems(roomList);
            
        } catch (SQLException e) {
            e.printStackTrace();
            showMessage("Error loading rooms: " + e.getMessage(), true);
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
} 