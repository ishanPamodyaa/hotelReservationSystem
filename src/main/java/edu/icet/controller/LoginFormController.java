package edu.icet.controller;

import edu.icet.Db.DBConnection;
import edu.icet.Model.User;
import edu.icet.Utill.Role;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.jasypt.util.text.BasicTextEncryptor;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class LoginFormController implements Initializable {

    @FXML
    private Label forgetPassword;

    @FXML
    private Button loginButton;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label signUp;

    @FXML
    private TextField usernameField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loginButton.setOnAction(this::handleLogin);
        loginButton.setOnMouseEntered(e -> 
            loginButton.setStyle("-fx-background-color: white; -fx-text-fill: #1e2b3c; -fx-border-width: 2px; -fx-border-color: white;"));
        
        loginButton.setOnMouseExited(e -> 
            loginButton.setStyle("-fx-background-color: transparent; -fx-border-width: 2px; -fx-border-color: white; -fx-text-fill: white;"));

        signUp.setOnMouseClicked(this::handleSignUp);
        forgetPassword.setOnMouseClicked(this::handleForgotPassword);
    }


    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection connection = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    String storedHash =resultSet.getString("password_hash");
                    if (BCrypt.checkpw(password, storedHash)) {
                        Role role = Role.valueOf(resultSet.getString(4));
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Dashboard.fxml"));
                        Parent root = loader.load();

                        DashboardController dashboardController = loader.getController();
                        dashboardController.setLoggedInUser(role);

                        Stage stage = (Stage) loginButton.getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.setTitle("Hotel Reservation System - Dashboard");
                        stage.show();

                    } else {
                        showError("Invalid username or password.");
                    }
                } else {
                    showError("User not found.");
                }
            } catch (IOException e) {
                System.err.println("Error loading dashboard: " + e.getMessage());
                e.printStackTrace();
                showError("Could not load dashboard. Please try again.");
            }

        }catch (SQLException e) {
            e.printStackTrace();
            showError("Database error.");
        }

    }

    private boolean isValidLogin(String username, String password) {
        return !username.isEmpty() && !password.isEmpty();
    }
    
    private void showError(String message) {
        System.err.println("Login error: " + message);
      }
    
    private void handleSignUp(MouseEvent event) {
        System.out.println("Sign Up clicked - feature not implemented yet");
        // TODO: Implement sign up functionality
    }
    
    private void handleForgotPassword(MouseEvent event) {
        System.out.println("Forgot Password clicked - feature not implemented yet");
        // TODO: Implement password recovery
    }
}
