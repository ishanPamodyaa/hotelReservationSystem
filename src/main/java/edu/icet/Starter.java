package edu.icet;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class Starter extends Application {

    public static void main(String[] args) {
        launch();
    }



    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Hotel Reservation System");
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/view/LoginForm.fxml"))));
//        stage.setResizable(true);
//        stage.centerOnScreen();
        stage.show();
    }
}
