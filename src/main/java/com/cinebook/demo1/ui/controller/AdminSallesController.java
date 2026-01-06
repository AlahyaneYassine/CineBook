package com.cinebook.demo1.ui.controller;

import com.cinebook.demo1.dao.DB;
import com.cinebook.demo1.dao.SalleDAO;
import com.cinebook.demo1.exception.DataAccessException;
import com.cinebook.demo1.model.Salle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.util.List;

public class AdminSallesController {

    @FXML private TableView<Salle> tableSalles;
    @FXML private TableColumn<Salle, String> colId;
    @FXML private TableColumn<Salle, Integer> colCapacite;
    @FXML private TableColumn<Salle, String> colType;

    @FXML private TextField idField;
    @FXML private TextField capaciteField;
    @FXML private TextField typeField;
    @FXML private Label messageLabel;

    private final ObservableList<Salle> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCapacite.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        tableSalles.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableSalles.setPlaceholder(new Label("Aucune salle."));
        tableSalles.setItems(data);

        tableSalles.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                idField.setText(selected.getId());
                capaciteField.setText(String.valueOf(selected.getCapacite()));
                typeField.setText(selected.getType());
            }
        });

        loadSalles();
    }

    @FXML
    public void onRefresh() {
        loadSalles();
        messageLabel.setText("✅ Liste rafraîchie.");
    }

    @FXML
    public void onCreate() {
        String id = trim(idField.getText());
        String capStr = trim(capaciteField.getText());
        String type = trim(typeField.getText());

        if (id.isBlank() || capStr.isBlank() || type.isBlank()) {
            messageLabel.setText("❌ Tous les champs sont obligatoires.");
            return;
        }

        int capacite;
        try {
            capacite = Integer.parseInt(capStr);
            if (capacite <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            messageLabel.setText("❌ Capacité invalide (entier > 0).");
            return;
        }

        try (Connection conn = DB.getConnection()) {
            SalleDAO dao = new SalleDAO(conn);

            if (dao.readSalleById(id) != null) {
                messageLabel.setText("❌ ID déjà utilisé.");
                return;
            }

            dao.createSalle(new Salle(id, capacite, type));

            loadSalles();
            clearForm();
            messageLabel.setText("✅ Salle ajoutée.");

        } catch (DataAccessException ex) {
            messageLabel.setText("❌ DAO : " + safeMsg(ex));
        } catch (Exception ex) {
            messageLabel.setText("❌ DB : " + safeMsg(ex));
        }
    }

    @FXML
    public void onUpdate() {
        Salle selected = tableSalles.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("❌ Sélectionne une salle dans le tableau.");
            return;
        }

        String id = trim(idField.getText());
        String capStr = trim(capaciteField.getText());
        String type = trim(typeField.getText());

        if (id.isBlank() || capStr.isBlank() || type.isBlank()) {
            messageLabel.setText("❌ Tous les champs sont obligatoires.");
            return;
        }

        if (!selected.getId().equals(id)) {
            messageLabel.setText("❌ L'ID ne peut pas être modifié. (Supprime + recrée si besoin)");
            return;
        }

        int capacite;
        try {
            capacite = Integer.parseInt(capStr);
            if (capacite <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            messageLabel.setText("❌ Capacité invalide (entier > 0).");
            return;
        }

        try (Connection conn = DB.getConnection()) {
            SalleDAO dao = new SalleDAO(conn);
            dao.updateSalle(new Salle(id, capacite, type));

            loadSalles();
            messageLabel.setText("✅ Salle modifiée.");

        } catch (DataAccessException ex) {
            messageLabel.setText("❌ DAO : " + safeMsg(ex));
        } catch (Exception ex) {
            messageLabel.setText("❌ DB : " + safeMsg(ex));
        }
    }

    @FXML
    public void onDelete() {
        Salle selected = tableSalles.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("❌ Sélectionne une salle à supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Supprimer la salle ?");
        confirm.setContentText("ID: " + selected.getId() + "\nCapacité: " + selected.getCapacite() + "\nType: " + selected.getType());
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try (Connection conn = DB.getConnection()) {
            SalleDAO dao = new SalleDAO(conn);
            dao.deleteSalle(selected.getId());

            loadSalles();
            clearForm();
            messageLabel.setText("✅ Salle supprimée.");

        } catch (DataAccessException ex) {
            messageLabel.setText("❌ DAO : " + safeMsg(ex));
        } catch (Exception ex) {
            messageLabel.setText("❌ DB : " + safeMsg(ex));
        }
    }

    // Navigation handled by AdminShellController

    // ============================
    // Helpers
    // ============================

    private void loadSalles() {
        messageLabel.setText("");
        try (Connection conn = DB.getConnection()) {
            SalleDAO dao = new SalleDAO(conn);
            List<Salle> list = dao.readAllSalles();
            data.setAll(list);
        } catch (Exception ex) {
            messageLabel.setText("❌ Chargement impossible: " + safeMsg(ex));
        }
    }

    private void clearForm() {
        idField.clear();
        capaciteField.clear();
        typeField.clear();
        tableSalles.getSelectionModel().clearSelection();
    }

    private String trim(String s) { return s == null ? "" : s.trim(); }

    private String safeMsg(Exception e) {
        if (e.getMessage() != null && !e.getMessage().isBlank()) return e.getMessage();
        return e.getClass().getSimpleName();
    }
}
