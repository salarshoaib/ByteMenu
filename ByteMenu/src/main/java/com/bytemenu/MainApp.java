package com.bytemenu;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialise DB on startup
        com.bytemenu.dao.DatabaseManager.getInstance();

        Parent root = FXMLLoader.load(getClass().getResource("/com/bytemenu/fxml/Login.fxml"));
        Scene scene = new Scene(root, 900, 600);

        primaryStage.setTitle("ByteMenu — Login");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
