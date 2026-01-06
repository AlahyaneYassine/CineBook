package com.cinebook.demo1.ui.controller;

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
import java.time.LocalDate;
import java.util.UUID;

public class SignupController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private Label messageLabel;

    @FXML
    public void onSignup(ActionEvent event) {
        String username = safe(usernameField.getText());
        String password = safe(passwordField.getText());
        String nom = safe(nomField.getText());
        String prenom = safe(prenomField.getText());
        String email = safe(emailField.getText());

        // ✅ validations
        if (username.isBlank() || password.isBlank()) {
            messageLabel.setText("Username et mot de passe sont obligatoires.");
            return;
        }
        if (username.length() < 3) {
            messageLabel.setText("Username trop court (min 3).");
            return;
        }
        if (password.length() < 4) {
            messageLabel.setText("Mot de passe trop court (min 4).");
            return;
        }
        if (!email.isBlank() && !email.contains("@")) {
            messageLabel.setText("Email invalide.");
            return;
        }

        try (Connection conn = DB.getConnection()) {
            UtilisateurDAO dao = new UtilisateurDAO(conn);

            // username unique
            Utilisateur exist = dao.readUtilisateurByUsername(username);
            if (exist != null) {
                messageLabel.setText("❌ Username déjà utilisé.");
                return;
            }

            Utilisateur newUser = new Utilisateur(
                    UUID.randomUUID().toString(),
                    username,
                    password,        // (hash plus tard si tu veux)
                    "CLIENT",
                    nom,
                    prenom,
                    email,
                    LocalDate.now()
            );

            dao.createUtilisateur(newUser);

            messageLabel.setStyle("-fx-text-fill: #0a0;");
            messageLabel.setText("✅ Compte créé ! Retour au login...");
            // Redirection login
            Navigator.navigateFromNode(usernameField, "/com/cinebook/demo1/login.fxml", "CineBook - Connexion", 520.0, 320.0);

        } catch (DataAccessException e) {
            messageLabel.setStyle("-fx-text-fill: #c00;");
            messageLabel.setText("Erreur DAO : " + safeMsg(e));
        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: #c00;");
            messageLabel.setText("Erreur DB : " + safeMsg(e));
        }
    }

    @FXML
    public void onBackToLogin(ActionEvent event) {
        Navigator.navigateFromNode(usernameField, "/com/cinebook/demo1/login.fxml", "CineBook - Connexion", 520.0, 320.0);
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private String safeMsg(Exception e) {
        if (e.getMessage() != null && !e.getMessage().isBlank()) return e.getMessage();
        return e.getClass().getSimpleName();
    }
}
