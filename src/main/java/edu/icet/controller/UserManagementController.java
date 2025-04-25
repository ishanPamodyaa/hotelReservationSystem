package edu.icet.controller;

import edu.icet.Db.DBConnection;
import edu.icet.Model.User;
import edu.icet.Utill.Role;
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
import org.mindrot.jbcrypt.BCrypt;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class UserManagementController implements Initializable {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private ComboBox<Role> roleComboBox;

    @FXML
    private Button addButton;

    @FXML
    private Button clearButton;

    @FXML
    private Label messageLabel;

    @FXML
    private TableView<User> userTable;

    @FXML
    private TableColumn<User, Integer> idColumn;

    @FXML
    private TableColumn<User, String> usernameColumn;

    @FXML
    private TableColumn<User, String> roleColumn;

    @FXML
    private TableColumn<User, Void> actionColumn;

    private ObservableList<User> userList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set up the role ComboBox
        roleComboBox.getItems().addAll(Role.Admin, Role.Manager, Role.Receptionist);
        
        // Set up table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRole().toString()));
        
        // Add action column (Delete button)
        addButtonToTable();
        
        // Load users from database
        loadUsers();
    }

    private void addButtonToTable() {
        Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactory = new Callback<TableColumn<User, Void>, TableCell<User, Void>>() {
            @Override
            public TableCell<User, Void> call(final TableColumn<User, Void> param) {
                return new TableCell<User, Void>() {
                    private final Button deleteButton = new Button("Delete");
                    {
                        deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
                        deleteButton.setOnAction((ActionEvent event) -> {
                            User user = getTableView().getItems().get(getIndex());
                            handleDeleteUser(user);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(deleteButton);
                        }
                    }
                };
            }
        };
        
        actionColumn.setCellFactory(cellFactory);
    }

    @FXML
    void handleAddUser(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        Role selectedRole = roleComboBox.getValue();
        
        // Validate inputs
        if (username.isEmpty()) {
            showMessage("Username cannot be empty", true);
            return;
        }
        
        if (password.isEmpty()) {
            showMessage("Password cannot be empty", true);
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showMessage("Passwords do not match", true);
            return;
        }
        
        if (selectedRole == null) {
            showMessage("Please select a role", true);
            return;
        }
        
        // Check if username already exists
        if (isUsernameExists(username)) {
            showMessage("Username already exists", true);
            return;
        }
        
        // Hash the password
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        
        // Add user to the database
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            if (connection == null) {
                showMessage("Unable to establish database connection", true);
                return;
            }
            
            String sql = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)";
            statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            statement.setString(2, hashedPassword);
            statement.setString(3, selectedRole.toString());
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                showMessage("User added successfully", false);
                clearFields();
                loadUsers(); // Refresh table
            } else {
                showMessage("Failed to add user", true);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showMessage("Database error: " + e.getMessage(), true);
        } finally {
            try {
                if (statement != null) statement.close();
                // Don't close connection here
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private boolean isUsernameExists(String username) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            if (connection == null) {
                showMessage("Unable to establish database connection", true);
                return false;
            }
            
            String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showMessage("Database error checking username: " + e.getMessage(), true);
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                // Don't close connection here
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return false;
    }

    @FXML
    void handleClear(ActionEvent event) {
        clearFields();
    }
    
    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        roleComboBox.setValue(null);
        messageLabel.setText("");
    }
    
    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.setTextFill(isError ? javafx.scene.paint.Color.RED : javafx.scene.paint.Color.GREEN);
    }

    @FXML
    void handleRefresh(ActionEvent event) {
        loadUsers();
    }
    
    private void handleDeleteUser(User user) {
        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete User");
        alert.setHeaderText("Delete User: " + user.getUsername());
        alert.setContentText("Are you sure you want to delete this user?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteUser(user);
            }
        });
    }
    
    private void deleteUser(User user) {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            if (connection == null) {
                showMessage("Unable to establish database connection", true);
                return;
            }
            
            String sql = "DELETE FROM users WHERE user_id = ?";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, user.getId());
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                showMessage("User deleted successfully", false);
                loadUsers(); // Refresh table
            } else {
                showMessage("Failed to delete user", true);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showMessage("Database error: " + e.getMessage(), true);
        } finally {
            try {
                if (statement != null) statement.close();
                // Don't close connection here, it's managed by DBConnection
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void loadUsers() {
        userList.clear();
        
        Connection connection = null;
        try {
            connection = DBConnection.getInstance().getConnection();
            if (connection == null) {
                showMessage("Unable to establish database connection", true);
                return;
            }
            
            String sql = "SELECT * FROM users";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
            
            while (resultSet.next()) {
                int id = resultSet.getInt("user_id");
                String username = resultSet.getString("username");
                String passwordHash = resultSet.getString("password_hash");
                Role role = Role.valueOf(resultSet.getString("role"));
                
                User user = new User(id, username, passwordHash, role);
                userList.add(user);
            }
            
            userTable.setItems(userList);
            
        } catch (SQLException e) {
            e.printStackTrace();
            showMessage("Error loading users: " + e.getMessage(), true);
        }
    }
} 