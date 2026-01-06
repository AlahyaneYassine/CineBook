package com.cinebook.demo1.ui.controller;

import com.cinebook.demo1.app.Session;
import com.cinebook.demo1.dao.DB;
import com.cinebook.demo1.dao.UtilisateurDAO;
import com.cinebook.demo1.exception.DataAccessException;
import com.cinebook.demo1.model.Utilisateur;
import com.cinebook.demo1.ui.navigation.Navigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.Connection;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    @FXML
    public void onLogin(ActionEvent event) {

        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            messageLabel.setText("Username et mot de passe requis.");
            return;
        }

        try (Connection conn = DB.getConnection()) {

            UtilisateurDAO utilisateurDAO = new UtilisateurDAO(conn);
            Utilisateur u = utilisateurDAO.readUtilisateurByUsername(username);

            if (u == null) {
                messageLabel.setText("Utilisateur introuvable.");
                return;
            }

            if (u.getPasswordHash() == null || !password.equals(u.getPasswordHash())) {
                messageLabel.setText("Mot de passe incorrect.");
                return;
            }

            // ✅ stocker en session
            Session.setCurrentUser(u);

            // ✅ redirection selon rôle - use shells
            if ("ADMIN".equalsIgnoreCase(u.getRole())) {
                Navigator.navigateFromNode(usernameField, "/com/cinebook/demo1/shell/adminShell.fxml", "CineBook - Admin", 1200.0, 800.0);
            } else {
                Navigator.navigateFromNode(usernameField, "/com/cinebook/demo1/shell/clientShell.fxml", "CineBook - Client", 1200.0, 800.0);
            }

            if (event != null) event.consume();

        } catch (DataAccessException e) {
            e.printStackTrace(); // utile en dev
            messageLabel.setText("Erreur DAO : " + safeMsg(e));
        } catch (Exception e) {
            e.printStackTrace(); // utile en dev
            messageLabel.setText("Erreur : " + safeMsg(e));
        }
    }

    @FXML
    public void onSignup(ActionEvent event) {
        Navigator.navigateFromNode(usernameField, "/com/cinebook/demo1/signup.fxml", "CineBook - Inscription", 800.0, 520.0);
    }


    private String safeMsg(Exception e) {
        if (e.getMessage() != null && !e.getMessage().isBlank()) return e.getMessage();
        return e.getClass().getSimpleName();
    }
}
