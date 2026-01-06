package com.cinebook.demo1.ui.controller;

import com.cinebook.demo1.app.Session;
import com.cinebook.demo1.dao.DB;
import com.cinebook.demo1.dao.UtilisateurDAO;
import com.cinebook.demo1.exception.DataAccessException;
import com.cinebook.demo1.model.Utilisateur;
import com.cinebook.demo1.ui.navigation.Navigator;
import com.cinebook.demo1.ui.navigation.ShellRouter;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.sql.Connection;
import java.time.LocalDate;

public class ProfileController {

    @FXML private TextField usernameField;
    @FXML private TextField roleField;

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;

    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private Label messageLabel;

    private Utilisateur current;

    private static final String FXML_SEANCES = "/com/cinebook/demo1/client/client.fxml";

    @FXML
    public void initialize() {
        current = Session.getCurrentUser();
        if (current == null) {
            setError("Session expirée. Reconnecte-toi.");
            disableAll();
            return;
        }

        usernameField.setText(nz(current.getUsername()));
        roleField.setText(nz(current.getRole()));

        nomField.setText(nz(current.getNom()));
        prenomField.setText(nz(current.getPrenom()));
        emailField.setText(nz(current.getEmail()));

        messageLabel.setText("");
    }

    // ✅ UN SEUL onBack()
    @FXML
    public void onBack() {
        StackPane pane = ShellRouter.getClientContentPane();
        if (pane == null) {
            setError("Navigation impossible : contentPane non initialisé (ShellRouter).");
            return;
        }
        Navigator.loadContent(FXML_SEANCES, pane);
    }

    @FXML
    public void onSave() {
        if (current == null) return;

        String nom = safe(nomField.getText());
        String prenom = safe(prenomField.getText());
        String email = safe(emailField.getText());

        String newPass = safe(newPasswordField.getText());
        String confirm = safe(confirmPasswordField.getText());

        if (!email.isBlank() && !email.contains("@")) {
            setError("Email invalide.");
            return;
        }

        if (!newPass.isBlank() || !confirm.isBlank()) {
            if (newPass.isBlank() || confirm.isBlank()) {
                setError("Confirme le mot de passe.");
                return;
            }
            if (!newPass.equals(confirm)) {
                setError("Les mots de passe ne correspondent pas.");
                return;
            }
            if (newPass.length() < 4) {
                setError("Mot de passe trop court (min 4).");
                return;
            }
        }

        current.setNom(nom);
        current.setPrenom(prenom);
        current.setEmail(email);

        if (!newPass.isBlank()) {
            current.setPasswordHash(newPass); // hash plus tard si tu veux
        }

        current.setLastProfileUpdate(LocalDate.now());

        try (Connection conn = DB.getConnection()) {
            UtilisateurDAO dao = new UtilisateurDAO(conn);
            dao.updateUtilisateur(current);

            Session.setCurrentUser(current);

            setSuccess("✅ Profil mis à jour.");
            newPasswordField.clear();
            confirmPasswordField.clear();

        } catch (DataAccessException e) {
            setError("Erreur DAO : " + safeMsg(e));
        } catch (Exception e) {
            setError("Erreur DB : " + safeMsg(e));
        }
    }

    @FXML
    public void onCancel() {
        if (current == null) return;

        nomField.setText(nz(current.getNom()));
        prenomField.setText(nz(current.getPrenom()));
        emailField.setText(nz(current.getEmail()));
        newPasswordField.clear();
        confirmPasswordField.clear();
        messageLabel.setText("");
    }

    // si ton FXML appelle onClose, on évite le crash et on revient
    @FXML
    public void onClose() {
        onBack();
    }

    private void disableAll() {
        usernameField.setDisable(true);
        roleField.setDisable(true);
        nomField.setDisable(true);
        prenomField.setDisable(true);
        emailField.setDisable(true);
        newPasswordField.setDisable(true);
        confirmPasswordField.setDisable(true);
    }

    private void setError(String msg) {
        messageLabel.setStyle("-fx-text-fill: #ef4444;");
        messageLabel.setText(msg);
    }

    private void setSuccess(String msg) {
        messageLabel.setStyle("-fx-text-fill: #22c55e;");
        messageLabel.setText(msg);
    }

    private String safe(String s) { return s == null ? "" : s.trim(); }
    private String nz(String s) { return s == null ? "" : s; }

    private String safeMsg(Exception e) {
        if (e.getMessage() != null && !e.getMessage().isBlank()) return e.getMessage();
        return e.getClass().getSimpleName();
    }
}
