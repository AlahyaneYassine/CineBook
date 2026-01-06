package com.cinebook.demo1.ui.controller;

import com.cinebook.demo1.dao.DB;
import com.cinebook.demo1.dao.FilmDAO;
import com.cinebook.demo1.model.Film;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.util.List;
import java.util.UUID;

public class AdminFilmsController {

    @FXML private TableView<Film> tableFilms;
    @FXML private TableColumn<Film, String> colId;
    @FXML private TableColumn<Film, String> colTitre;
    @FXML private TableColumn<Film, String> colGenre;
    @FXML private TableColumn<Film, Integer> colDuree;
    @FXML private TableColumn<Film, Integer> colAge;

    @FXML private TextField titreField;
    @FXML private TextField genreField;
    @FXML private TextField dureeField;
    @FXML private TextField ageField;

    @FXML private Label messageLabel;

    private final ObservableList<Film> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Colonnes
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("duree"));
        colAge.setCellValueFactory(new PropertyValueFactory<>("ageRestriction"));

        tableFilms.setItems(data);

        // Sélection => remplit le formulaire
        tableFilms.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                titreField.setText(nvl(selected.getTitre()));
                genreField.setText(nvl(selected.getGenre()));
                dureeField.setText(String.valueOf(selected.getDuree()));
                ageField.setText(String.valueOf(selected.getAgeRestriction()));
                messageLabel.setText("");
            }
        });

        loadFilms();
    }

    // ======================
    //        ACTIONS
    // ======================

    @FXML
    public void onRefresh() {
        loadFilms();
        messageLabel.setText("");
    }

    @FXML
    public void onAdd() {
        try {
            Film film = buildFilmFromFields(UUID.randomUUID().toString());

            try (Connection conn = DB.getConnection()) {
                new FilmDAO(conn).createFilm(film);
            }

            messageLabel.setText("✅ Film ajouté.");
            loadFilms();
            clearForm();

        } catch (Exception e) {
            messageLabel.setText("❌ " + safeMsg(e));
        }
    }

    @FXML
    public void onUpdate() {
        Film selected = tableFilms.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("❌ Sélectionne un film à modifier.");
            return;
        }

        try {
            // Met à jour les champs (Film doit avoir des setters)
            selected.setTitre(requiredText(titreField, "Titre"));
            selected.setGenre(requiredText(genreField, "Genre"));
            selected.setDuree(requiredInt(dureeField, "Durée (minutes)", 1, 10000));
            selected.setAgeRestriction(requiredInt(ageField, "Âge minimum", 0, 99));

            try (Connection conn = DB.getConnection()) {
                new FilmDAO(conn).updateFilm(selected);
            }

            messageLabel.setText("✅ Film modifié.");
            loadFilms();

        } catch (Exception e) {
            messageLabel.setText("❌ " + safeMsg(e));
        }
    }

    @FXML
    public void onDelete() {
        Film selected = tableFilms.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("❌ Sélectionne un film à supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Supprimer ce film ?");
        confirm.setContentText(
                "Titre : " + selected.getTitre() +
                        "\nGenre : " + selected.getGenre() +
                        "\nDurée : " + selected.getDuree() +
                        "\nÂge min : " + selected.getAgeRestriction()
        );

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try (Connection conn = DB.getConnection()) {
            new FilmDAO(conn).deleteFilm(selected.getId());
            messageLabel.setText("✅ Film supprimé.");
            loadFilms();
            clearForm();
        } catch (Exception e) {
            messageLabel.setText("❌ " + safeMsg(e));
        }
    }

    @FXML
    public void onClear() {
        clearForm();
        tableFilms.getSelectionModel().clearSelection();
        messageLabel.setText("");
    }

    // ======================
    //        LOAD
    // ======================

    private void loadFilms() {
        try (Connection conn = DB.getConnection()) {
            FilmDAO dao = new FilmDAO(conn);
            List<Film> films = dao.readAllFilms();
            data.setAll(films);
            messageLabel.setText("✅ " + films.size() + " films chargés.");
        } catch (Exception e) {
            messageLabel.setText("❌ Erreur chargement : " + safeMsg(e));
        }
    }

    // ======================
    //      HELPERS FORM
    // ======================

    private Film buildFilmFromFields(String id) {
        String titre = requiredText(titreField, "Titre");
        String genre = requiredText(genreField, "Genre");
        int duree = requiredInt(dureeField, "Durée (minutes)", 1, 10000);
        int age = requiredInt(ageField, "Âge minimum", 0, 99);

        return new Film(id, titre, genre, duree, age);
    }

    private void clearForm() {
        titreField.clear();
        genreField.clear();
        dureeField.clear();
        ageField.clear();
    }

    private String requiredText(TextField field, String label) {
        String v = field.getText();
        if (v == null || v.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " est obligatoire.");
        }
        return v.trim();
    }

    private int requiredInt(TextField field, String label, int min, int max) {
        String v = requiredText(field, label);
        try {
            int n = Integer.parseInt(v);
            if (n < min || n > max) {
                throw new IllegalArgumentException(label + " doit être entre " + min + " et " + max + ".");
            }
            return n;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " doit être un nombre.");
        }
    }

    private String safeMsg(Exception e) {
        if (e.getMessage() != null && !e.getMessage().isBlank()) return e.getMessage();
        return e.getClass().getSimpleName();
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }
}
