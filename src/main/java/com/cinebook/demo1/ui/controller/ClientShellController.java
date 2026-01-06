package com.cinebook.demo1.ui.controller;
import com.cinebook.demo1.ui.navigation.ShellRouter;

import java.net.URL;
import java.util.ResourceBundle;

import com.cinebook.demo1.app.Session;
import com.cinebook.demo1.model.Utilisateur;
import com.cinebook.demo1.ui.navigation.Navigator;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Shell controller for Client interface.
 * Manages navigation between client pages within a single window.
 */
public class ClientShellController implements Initializable {

    @FXML private StackPane contentPane;
    @FXML private Button seancesBtn;
    @FXML private Button reservationsBtn;
    @FXML private Button profileBtn;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;

    private Button activeButton;

    // Chemins centralisés (évite les fautes de frappe)
    private static final String FXML_SEANCES = "/com/cinebook/demo1/client/client.fxml";
    private static final String FXML_RESERVATIONS = "/com/cinebook/demo1/client/mesReservations.fxml";
    private static final String FXML_PROFILE = "/com/cinebook/demo1/client/profil.fxml";
    private static final String FXML_LOGIN = "/com/cinebook/demo1/login.fxml";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1) Remplir infos user (si session ok)
        ShellRouter.setClientContentPane(contentPane);
        Utilisateur user = Session.getCurrentUser();
        if (user != null) {
            userNameLabel.setText(user.getUsername());
            userRoleLabel.setText(user.getRole() != null ? user.getRole().toUpperCase() : "CLIENT");
        } else {
            userNameLabel.setText("Invité");
            userRoleLabel.setText("CLIENT");
        }

        // 2) Page par défaut (séances)
        activeButton = null;
        goSeances();
    }

    private void setActiveButton(Button button) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("active");
        }
        activeButton = button;
        if (activeButton != null && !activeButton.getStyleClass().contains("active")) {
            activeButton.getStyleClass().add("active");
        }
    }

    private Stage getStage() {
        if (contentPane == null || contentPane.getScene() == null) return null;
        return (Stage) contentPane.getScene().getWindow();
    }

    // ======= Navigation interne (helpers) =======

    private void goSeances() {
        setActiveButton(seancesBtn);
        Navigator.loadContent(FXML_SEANCES, contentPane);
    }

    private void goReservations() {
        setActiveButton(reservationsBtn);
        Navigator.loadContent(FXML_RESERVATIONS, contentPane);
    }

    // ======= Handlers FXML =======

    @FXML
    public void onNavigateSeances(ActionEvent event) {
        goSeances();
        if (event != null) event.consume();
    }

    @FXML
    public void onNavigateReservations(ActionEvent event) {
        goReservations();
        if (event != null) event.consume();
    }

    @FXML
    public void onNavigateProfile(ActionEvent event) {
        setActiveButton(profileBtn);
        Navigator.loadContent(FXML_PROFILE, contentPane);   // ✅ même fenêtre
        if (event != null) event.consume();
    }


    @FXML
    public void onLogout(ActionEvent event) {
        Session.clear();
        Navigator.navigateFromNode(contentPane, FXML_LOGIN, "CineBook - Connexion", 520.0, 320.0);
        if (event != null) event.consume();
    }
}
