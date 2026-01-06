package com.cinebook.demo1.ui.controller;

import com.cinebook.demo1.app.Session;
import com.cinebook.demo1.model.Utilisateur;
import com.cinebook.demo1.ui.navigation.Navigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Shell controller for Admin interface.
 * Manages navigation between admin pages within a single window.
 */
public class AdminShellController implements Initializable {
    
    @FXML private StackPane contentPane;
    @FXML private Button filmsBtn;
    @FXML private Button sallesBtn;
    @FXML private Button seancesBtn;
    @FXML private Button reservationsBtn;
    @FXML private Button usersBtn;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    
    private Button activeButton;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Load user info
        Utilisateur user = Session.getCurrentUser();
        if (user != null) {
            userNameLabel.setText(user.getUsername());
            userRoleLabel.setText("ADMIN");
        }
        
        // Set initial active button (none)
        activeButton = null;
    }
    
    /**
     * Set active navigation button.
     */
    private void setActiveButton(Button button) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("active");
        }
        activeButton = button;
        if (button != null) {
            button.getStyleClass().add("active");
        }
    }
    
    @FXML
    public void onNavigateFilms(ActionEvent event) {
        setActiveButton(filmsBtn);
        Navigator.loadContent("/com/cinebook/demo1/admin/adminFilms.fxml", contentPane);
    }
    
    @FXML
    public void onNavigateSalles(ActionEvent event) {
        setActiveButton(sallesBtn);
        Navigator.loadContent("/com/cinebook/demo1/admin/adminSalles.fxml", contentPane);
    }
    
    @FXML
    public void onNavigateSeances(ActionEvent event) {
        setActiveButton(seancesBtn);
        Navigator.loadContent("/com/cinebook/demo1/admin/adminSeances.fxml", contentPane);
    }
    
    @FXML
    public void onNavigateReservations(ActionEvent event) {
        setActiveButton(reservationsBtn);
        Navigator.loadContent("/com/cinebook/demo1/admin/adminReservations.fxml", contentPane);
    }
    
    @FXML
    public void onNavigateUsers(ActionEvent event) {
        setActiveButton(usersBtn);
        Navigator.loadContent("/com/cinebook/demo1/admin/adminUsers.fxml", contentPane);
    }
    
    @FXML
    public void onLogout(ActionEvent event) {
        Session.clear();
        Navigator.navigateFromNode(contentPane, "/com/cinebook/demo1/login.fxml", "CineBook - Connexion", 520.0, 320.0);
    }
}

