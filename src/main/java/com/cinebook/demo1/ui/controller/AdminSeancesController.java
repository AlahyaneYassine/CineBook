package com.cinebook.demo1.ui.controller;

import com.cinebook.demo1.dao.DB;
import com.cinebook.demo1.dao.FilmDAO;
import com.cinebook.demo1.dao.SalleDAO;
import com.cinebook.demo1.dao.SeanceDAO;
import com.cinebook.demo1.model.Film;
import com.cinebook.demo1.model.Salle;
import com.cinebook.demo1.model.Seance;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class AdminSeancesController {

    @FXML private TableView<Seance> seancesTable;

    @FXML private TableColumn<Seance, String> colId;
    @FXML private TableColumn<Seance, String> colFilm;
    @FXML private TableColumn<Seance, String> colSalle;
    @FXML private TableColumn<Seance, String> colDate;
    @FXML private TableColumn<Seance, String> colHeure;
    @FXML private TableColumn<Seance, String> colTarif;

    @FXML private ComboBox<Film> filmCombo;
    @FXML private ComboBox<Salle> salleCombo;
    @FXML private DatePicker datePicker;
    @FXML private TextField heureField;
    @FXML private TextField tarifField;

    @FXML private Label messageLabel;

    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        seancesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        seancesTable.setPlaceholder(new Label("Aucune séance trouvée."));

        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getId()));

        colFilm.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getFilm() == null ? "" : d.getValue().getFilm().getTitre()
        ));

        colSalle.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getSalle() == null ? "" : d.getValue().getSalle().getId()
        ));

        colDate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDate() == null ? "" : d.getValue().getDate().toString()
        ));

        colHeure.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getHeure() == null ? "" : d.getValue().getHeure().format(timeFmt)
        ));

        colTarif.setCellValueFactory(d -> new SimpleStringProperty(
                String.format(Locale.US, "%.2f", d.getValue().getTarif())
        ));

        filmCombo.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(Film item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getTitre());
            }
        });
        filmCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Film item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getTitre());
            }
        });

        salleCombo.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(Salle item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : ("Salle " + item.getId() + " (" + item.getCapacite() + " places)"));
            }
        });
        salleCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Salle item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : ("Salle " + item.getId() + " (" + item.getCapacite() + " places)"));
            }
        });

        // ✅ sélection => remplir formulaire (par ID, robuste)
        seancesTable.getSelectionModel().selectedItemProperty().addListener((obs, old, s) -> {
            if (s == null) return;

            selectFilmById(s.getFilm() == null ? null : s.getFilm().getId());
            selectSalleById(s.getSalle() == null ? null : s.getSalle().getId());

            datePicker.setValue(s.getDate());
            heureField.setText(s.getHeure() == null ? "" : s.getHeure().format(timeFmt));
            tarifField.setText(String.valueOf(s.getTarif()));
            messageLabel.setText("");
        });

        loadCombos();
        loadTable();
    }

    private void selectFilmById(String filmId) {
        if (filmId == null) { filmCombo.setValue(null); return; }
        for (Film f : filmCombo.getItems()) {
            if (filmId.equals(f.getId())) { filmCombo.setValue(f); return; }
        }
        filmCombo.setValue(null);
    }

    private void selectSalleById(String salleId) {
        if (salleId == null) { salleCombo.setValue(null); return; }
        for (Salle s : salleCombo.getItems()) {
            if (salleId.equals(s.getId())) { salleCombo.setValue(s); return; }
        }
        salleCombo.setValue(null);
    }

    private void loadCombos() {
        try (Connection conn = DB.getConnection()) {
            List<Film> films = new FilmDAO(conn).readAllFilms();
            List<Salle> salles = new SalleDAO(conn).readAllSalles();

            filmCombo.setItems(FXCollections.observableArrayList(films));
            salleCombo.setItems(FXCollections.observableArrayList(salles));
        } catch (Exception e) {
            messageLabel.setText("Erreur chargement films/salles : " + e.getMessage());
        }
    }

    private void loadTable() {
        try (Connection conn = DB.getConnection()) {
            List<Seance> seances = new SeanceDAO(conn).readAllSeances();
            seancesTable.setItems(FXCollections.observableArrayList(seances));
            messageLabel.setText("✅ " + seances.size() + " séance(s) chargée(s).");
        } catch (Exception e) {
            messageLabel.setText("Erreur chargement séances : " + e.getMessage());
        }
    }

    @FXML
    public void onRefresh() {
        loadCombos();
        loadTable();
        messageLabel.setText("");
    }

    @FXML
    public void onClear() {
        filmCombo.setValue(null);
        salleCombo.setValue(null);
        datePicker.setValue(null);
        heureField.clear();
        tarifField.clear();
        seancesTable.getSelectionModel().clearSelection();
        messageLabel.setText("");
    }

    @FXML
    public void onCreate() {
        try {
            Film film = filmCombo.getValue();
            Salle salle = salleCombo.getValue();
            LocalDate date = datePicker.getValue();

            if (film == null || salle == null || date == null) {
                messageLabel.setText("Film, salle et date sont obligatoires.");
                return;
            }

            LocalTime heure = parseHeureStrict(heureField.getText());
            double tarif = parseTarif(tarifField.getText());

            Seance s = new Seance(UUID.randomUUID().toString(), film, salle, date, heure, tarif);

            try (Connection conn = DB.getConnection()) {
                new SeanceDAO(conn).createSeance(s);
            }

            loadTable();
            onClear();
            messageLabel.setText("✅ Séance ajoutée.");

        } catch (Exception e) {
            messageLabel.setText("Erreur ajout : " + e.getMessage());
        }
    }

    @FXML
    public void onUpdate() {
        Seance selected = seancesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Sélectionne une séance à modifier.");
            return;
        }

        try {
            Film film = filmCombo.getValue();
            Salle salle = salleCombo.getValue();
            LocalDate date = datePicker.getValue();

            if (film == null || salle == null || date == null) {
                messageLabel.setText("Film, salle et date sont obligatoires.");
                return;
            }

            LocalTime heure = parseHeureStrict(heureField.getText());
            double tarif = parseTarif(tarifField.getText());

            Seance updated = new Seance(selected.getId(), film, salle, date, heure, tarif);

            try (Connection conn = DB.getConnection()) {
                new SeanceDAO(conn).updateSeance(updated);
            }

            loadTable();
            messageLabel.setText("✏️ Séance mise à jour.");

        } catch (Exception e) {
            messageLabel.setText("Erreur update : " + e.getMessage());
        }
    }

    @FXML
    public void onDelete() {
        Seance selected = seancesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Sélectionne une séance à supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Supprimer la séance ?");
        confirm.setContentText(
                "Film : " + (selected.getFilm() != null ? selected.getFilm().getTitre() : "")
                        + "\nDate : " + selected.getDate() + " " + selected.getHeure()
        );

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try (Connection conn = DB.getConnection()) {
            new SeanceDAO(conn).deleteSeance(selected.getId());
            loadTable();
            onClear();
            messageLabel.setText("🗑️ Séance supprimée.");
        } catch (Exception e) {
            messageLabel.setText("Erreur suppression : " + e.getMessage());
        }
    }

    // Navigation handled by AdminShellController

    private LocalTime parseHeureStrict(String txt) {
        if (txt == null || txt.isBlank()) throw new IllegalArgumentException("Heure obligatoire (HH:mm).");
        try {
            return LocalTime.parse(txt.trim(), timeFmt);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Heure invalide. Format attendu : HH:mm (ex: 20:30)");
        }
    }

    private double parseTarif(String txt) {
        if (txt == null || txt.isBlank()) throw new IllegalArgumentException("Tarif obligatoire.");
        try {
            return Double.parseDouble(txt.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Tarif invalide (ex: 45.00).");
        }
    }
}
