package com.cinebook.demo1.ui.controller;

import com.cinebook.demo1.ui.navigation.Navigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;

public class AdminController {

    // ======= BOUTONS MENU ADMIN =======

    @FXML
    public void onManageFilms(ActionEvent e) {
        Navigator.navigateFromNode((Node) e.getSource(), "/com/cinebook/demo1/admin/adminFilms.fxml", "CineBook - Admin Films", 900.0, 600.0);
    }

    @FXML
    public void onManageSalles(ActionEvent e) {
        Navigator.navigateFromNode((Node) e.getSource(), "/com/cinebook/demo1/admin/adminSalles.fxml", "CineBook - Admin Salles", 900.0, 600.0);
    }

    @FXML
    public void onManageUsers(ActionEvent e) {
        Navigator.navigateFromNode((Node) e.getSource(), "/com/cinebook/demo1/admin/adminUsers.fxml", "CineBook - Admin Utilisateurs", 900.0, 600.0);
    }

    @FXML
    public void onManageSeances(ActionEvent e) {
        Navigator.navigateFromNode((Node) e.getSource(), "/com/cinebook/demo1/admin/adminSeances.fxml", "CineBook - Admin Séances", 900.0, 600.0);
    }

    @FXML
    public void onManageReservations(ActionEvent e) {
        Navigator.navigateFromNode((Node) e.getSource(), "/com/cinebook/demo1/admin/adminReservations.fxml", "CineBook - Admin Réservations", 1000.0, 650.0);
    }

    // ======= LOGOUT =======

    @FXML
    public void onLogout(ActionEvent event) {
        Navigator.navigateFromNode((Node) event.getSource(), "/com/cinebook/demo1/login.fxml", "CineBook - Connexion", 520.0, 320.0);
    }
}
