package com.cinebook.demo1.ui;

import com.cinebook.demo1.ui.navigation.Navigator;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Initialize Navigator with primary stage
        Navigator.initialize(stage);
        
        // Navigate to login screen
        Navigator.navigateTo("/com/cinebook/demo1/login.fxml", "CineBook", 520.0, 320.0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
