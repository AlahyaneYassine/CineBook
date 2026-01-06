package com.cinebook.demo1.ui.controller;

import com.cinebook.demo1.dao.DB;
import com.cinebook.demo1.dao.UtilisateurDAO;
import com.cinebook.demo1.exception.DataAccessException;
import com.cinebook.demo1.model.Utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class AdminUsersController {

    @FXML private TableView<Utilisateur> tableUsers;
    @FXML private TableColumn<Utilisateur, String> colId;
    @FXML private TableColumn<Utilisateur, String> colUsername;
    @FXML private TableColumn<Utilisateur, String> colRole;
    @FXML private TableColumn<Utilisateur, String> colNom;
    @FXML private TableColumn<Utilisateur, String> colPrenom;
    @FXML private TableColumn<Utilisateur, String> colEmail;

    @FXML private TextField idField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private Label messageLabel;

    private final ObservableList<Utilisateur> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Colonnes
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        tableUsers.setItems(data);
        tableUsers.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableUsers.setPlaceholder(new Label("Aucun utilisateur."));

        // Role combo
        roleCombo.setItems(FXCollections.observableArrayList("ADMIN", "CLIENT"));

        // ID auto -> on le bloque
        idField.setDisable(true);

        // Sélection => remplir formulaire (SANS password)
        tableUsers.getSelectionModel().selectedItemProperty().addListener((obs, old, u) -> {
            if (u == null) return;

            idField.setText(u.getId());
            usernameField.setText(u.getUsername());
            roleCombo.setValue(u.getRole());
            nomField.setText(nullToEmpty(u.getNom()));
            prenomField.setText(nullToEmpty(u.getPrenom()));
            emailField.setText(nullToEmpty(u.getEmail()));

            // jamais afficher le password/hash
            passwordField.clear();

            messageLabel.setText("");
        });

        loadUsers();
    }

    @FXML
    public void onRefresh(ActionEvent e) {
        loadUsers();
        messageLabel.setText("✅ Liste rafraîchie.");
    }

    @FXML
    public void onCreate(ActionEvent e) {
        String username = safe(usernameField.getText());
        String pass = safe(passwordField.getText());
        String role = roleCombo.getValue(); // ADMIN/CLIENT
        String nom = safe(nomField.getText());
        String prenom = safe(prenomField.getText());
        String email = safe(emailField.getText());

        if (username.isBlank() || pass.isBlank() || role == null || role.isBlank()) {
            messageLabel.setText("❌ Username, password et role sont obligatoires.");
            return;
        }

        String id = UUID.randomUUID().toString();

        try (Connection conn = DB.getConnection()) {
            UtilisateurDAO dao = new UtilisateurDAO(conn);

            if (dao.readUtilisateurByUsername(username) != null) {
                messageLabel.setText("❌ Username déjà utilisé.");
                return;
            }

            Utilisateur u = new Utilisateur(
                    id, username, pass, role,
                    nom, prenom, email, LocalDate.now()
            );

            dao.createUtilisateur(u);

            loadUsers();
            clearForm();
            messageLabel.setText("✅ Utilisateur ajouté.");

        } catch (DataAccessException ex) {
            messageLabel.setText("❌ DAO : " + safeMsg(ex));
        } catch (Exception ex) {
            messageLabel.setText("❌ DB : " + safeMsg(ex));
        }
    }

    @FXML
    public void onUpdate(ActionEvent e) {
        Utilisateur selected = tableUsers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("❌ Sélectionne un utilisateur.");
            return;
        }

        // username immuable
        String username = safe(usernameField.getText());
        if (!selected.getUsername().equals(username)) {
            messageLabel.setText("❌ Username est immuable (interdit de modifier).");
            usernameField.setText(selected.getUsername());
            return;
        }

        String role = roleCombo.getValue();
        if (role == null || role.isBlank()) {
            messageLabel.setText("❌ Role obligatoire (ADMIN ou CLIENT).");
            return;
        }

        selected.setRole(role);
        selected.setNom(safe(nomField.getText()));
        selected.setPrenom(safe(prenomField.getText()));
        selected.setEmail(safe(emailField.getText()));
        selected.setLastProfileUpdate(LocalDate.now());

        // update password uniquement si rempli
        String newPass = safe(passwordField.getText());
        if (!newPass.isBlank()) {
            selected.setPasswordHash(newPass);
        }

        try (Connection conn = DB.getConnection()) {
            UtilisateurDAO dao = new UtilisateurDAO(conn);
            dao.updateUtilisateur(selected);

            loadUsers();
            passwordField.clear();
            messageLabel.setText("✅ Utilisateur modifié.");

        } catch (DataAccessException ex) {
            messageLabel.setText("❌ DAO : " + safeMsg(ex));
        } catch (Exception ex) {
            messageLabel.setText("❌ DB : " + safeMsg(ex));
        }
    }

    @FXML
    public void onDelete(ActionEvent e) {
        Utilisateur selected = tableUsers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("❌ Sélectionne un utilisateur.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Supprimer l'utilisateur ?");
        confirm.setContentText("Username: " + selected.getUsername());
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try (Connection conn = DB.getConnection()) {
            UtilisateurDAO dao = new UtilisateurDAO(conn);
            dao.deleteUtilisateur(selected.getId());

            loadUsers();
            clearForm();
            messageLabel.setText("✅ Utilisateur supprimé.");

        } catch (DataAccessException ex) {
            messageLabel.setText("❌ DAO : " + safeMsg(ex));
        } catch (Exception ex) {
            messageLabel.setText("❌ DB : " + safeMsg(ex));
        }
    }

    // Navigation handled by AdminShellController

    private void loadUsers() {
        data.clear();
        try (Connection conn = DB.getConnection()) {
            UtilisateurDAO dao = new UtilisateurDAO(conn);
            List<Utilisateur> list = dao.readAllUtilisateurs();
            data.addAll(list);
        } catch (Exception ex) {
            messageLabel.setText("❌ Chargement impossible: " + safeMsg(ex));
        }
    }

    private void clearForm() {
        idField.clear();
        usernameField.clear();
        passwordField.clear();
        roleCombo.setValue(null);
        nomField.clear();
        prenomField.clear();
        emailField.clear();
        tableUsers.getSelectionModel().clearSelection();
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private String safeMsg(Exception e) {
        return (e.getMessage() != null && !e.getMessage().isBlank())
                ? e.getMessage()
                : e.getClass().getSimpleName();
    }
}
