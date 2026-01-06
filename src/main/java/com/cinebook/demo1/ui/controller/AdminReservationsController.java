package com.cinebook.demo1.ui.controller;

import com.cinebook.demo1.dao.DB;
import com.cinebook.demo1.dao.ReservationDAO;
import com.cinebook.demo1.dao.SeanceDAO;
import com.cinebook.demo1.dao.UtilisateurDAO;
import com.cinebook.demo1.exception.DataAccessException;
import com.cinebook.demo1.model.Reservation;
import com.cinebook.demo1.model.Seance;
import com.cinebook.demo1.model.Utilisateur;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AdminReservationsController {

    @FXML private TableView<Reservation> reservationsTable;

    @FXML private TableColumn<Reservation, String> colId;
    @FXML private TableColumn<Reservation, String> colUser;
    @FXML private TableColumn<Reservation, String> colSeance;
    @FXML private TableColumn<Reservation, String> colPlaces;
    @FXML private TableColumn<Reservation, String> colDateRes;

    @FXML private ComboBox<Utilisateur> userCombo;
    @FXML private ComboBox<Seance> seanceCombo;
    @FXML private TextField placesField;

    @FXML private Label messageLabel;

    private final DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {

        // ===== Colonnes =====
        colId.setCellValueFactory(d -> new SimpleStringProperty(nvl(d.getValue().getId())));

        colUser.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getUser() != null ? nvl(d.getValue().getUser().getUsername()) : "(user?)"
        ));

        colSeance.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getSeance() != null ? formatSeance(d.getValue().getSeance()) : "(séance?)"
        ));

        colPlaces.setCellValueFactory(d -> new SimpleStringProperty(formatPlaces(d.getValue().getPlaces())));

        colDateRes.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDateReservation() != null
                        ? d.getValue().getDateReservation().format(dtFmt)
                        : "(date?)"
        ));

        // ===== Combo affichage =====
        userCombo.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(Utilisateur item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : (item.getUsername() + " (" + item.getRole() + ")"));
            }
        });
        userCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Utilisateur item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : (item.getUsername() + " (" + item.getRole() + ")"));
            }
        });

        seanceCombo.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(Seance item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : formatSeance(item));
            }
        });
        seanceCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Seance item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : formatSeance(item));
            }
        });

        // ===== sélection table => remplir formulaire =====
        reservationsTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                userCombo.setValue(sel.getUser());
                seanceCombo.setValue(sel.getSeance());
                placesField.setText(sel.getPlaces() == null ? "" :
                        sel.getPlaces().stream().map(String::valueOf).collect(Collectors.joining(",")));
                messageLabel.setText("");
            }
        });

        loadCombos();
        loadTable();
    }

    // ============================
    //            LOAD
    // ============================

    private void loadCombos() {
        try (Connection conn = DB.getConnection()) {
            UtilisateurDAO uDao = new UtilisateurDAO(conn);
            SeanceDAO sDao = new SeanceDAO(conn);

            List<Utilisateur> users = uDao.readAllUtilisateurs();
            List<Seance> seances = sDao.readAllSeances();

            userCombo.setItems(FXCollections.observableArrayList(users));
            seanceCombo.setItems(FXCollections.observableArrayList(seances));

        } catch (DataAccessException e) {
            messageLabel.setText("Erreur DAO combos : " + safeMsg(e));
        } catch (SQLException e) {
            messageLabel.setText("Erreur DB combos : " + safeMsg(e));
        } catch (Exception e) {
            messageLabel.setText("Erreur combos : " + safeMsg(e));
        }
    }

    private void loadTable() {
        try (Connection conn = DB.getConnection()) {
            ReservationDAO dao = new ReservationDAO(conn);
            List<Reservation> list = dao.readAllReservations();
            reservationsTable.setItems(FXCollections.observableArrayList(list));
        } catch (DataAccessException e) {
            messageLabel.setText("Erreur DAO réservations : " + safeMsg(e));
        } catch (SQLException e) {
            messageLabel.setText("Erreur DB réservations : " + safeMsg(e));
        } catch (Exception e) {
            messageLabel.setText("Erreur réservations : " + safeMsg(e));
        }
    }

    // ============================
    //           ACTIONS
    // ============================

    @FXML
    public void onRefresh() {
        loadCombos();
        loadTable();
        messageLabel.setText("");
    }

    @FXML
    public void onClear() {
        userCombo.setValue(null);
        seanceCombo.setValue(null);
        placesField.clear();
        reservationsTable.getSelectionModel().clearSelection();
        messageLabel.setText("");
    }

    @FXML
    public void onCreate() {
        try {
            Utilisateur user = userCombo.getValue();
            Seance seance = seanceCombo.getValue();
            if (user == null || seance == null) {
                messageLabel.setText("Utilisateur et séance sont obligatoires.");
                return;
            }

            List<Integer> places = parsePlaces(placesField.getText());
            validatePlacesAgainstSalle(seance, places);

            Reservation r = new Reservation(
                    UUID.randomUUID().toString(),
                    user,
                    seance,
                    places,
                    LocalDateTime.now()
            );

            try (Connection conn = DB.getConnection()) {
                ReservationDAO dao = new ReservationDAO(conn);
                dao.createReservation(r);
            }

            onRefresh();
            onClear();
            messageLabel.setText("✅ Réservation créée.");
        } catch (DataAccessException e) {
            messageLabel.setText("Erreur DAO création : " + safeMsg(e));
        } catch (SQLException e) {
            messageLabel.setText("Erreur DB création : " + safeMsg(e));
        } catch (Exception e) {
            messageLabel.setText("Erreur création : " + safeMsg(e));
        }
    }

    @FXML
    public void onUpdate() {
        Reservation selected = reservationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Sélectionne une réservation à modifier.");
            return;
        }

        try {
            Utilisateur user = userCombo.getValue();
            Seance seance = seanceCombo.getValue();
            if (user == null || seance == null) {
                messageLabel.setText("Utilisateur et séance sont obligatoires.");
                return;
            }

            List<Integer> places = parsePlaces(placesField.getText());
            validatePlacesAgainstSalle(seance, places);

            Reservation updated = new Reservation(
                    selected.getId(),
                    user,
                    seance,
                    places,
                    selected.getDateReservation() // on garde l'ancienne date
            );

            try (Connection conn = DB.getConnection()) {
                ReservationDAO dao = new ReservationDAO(conn);
                dao.updateReservation(updated);
            }

            onRefresh();
            messageLabel.setText("✏️ Réservation mise à jour.");
        } catch (DataAccessException e) {
            messageLabel.setText("Erreur DAO update : " + safeMsg(e));
        } catch (SQLException e) {
            messageLabel.setText("Erreur DB update : " + safeMsg(e));
        } catch (Exception e) {
            messageLabel.setText("Erreur update : " + safeMsg(e));
        }
    }

    @FXML
    public void onDelete() {
        Reservation selected = reservationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Sélectionne une réservation à supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Supprimer la réservation ?");
        confirm.setContentText(
                "User: " + (selected.getUser() != null ? selected.getUser().getUsername() : "?")
                        + "\nSéance: " + (selected.getSeance() != null ? formatSeance(selected.getSeance()) : "?")
                        + "\nPlaces: " + formatPlaces(selected.getPlaces())
        );

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try (Connection conn = DB.getConnection()) {
            ReservationDAO dao = new ReservationDAO(conn);
            dao.deleteReservation(selected.getId());
        } catch (DataAccessException e) {
            messageLabel.setText("Erreur DAO suppression : " + safeMsg(e));
            return;
        } catch (SQLException e) {
            messageLabel.setText("Erreur DB suppression : " + safeMsg(e));
            return;
        } catch (Exception e) {
            messageLabel.setText("Erreur suppression : " + safeMsg(e));
            return;
        }

        onRefresh();
        messageLabel.setText("🗑️ Réservation supprimée.");
    }

    // Navigation handled by AdminShellController

    // ============================
    //            HELPERS
    // ============================

    private String formatSeance(Seance s) {
        return s.getFilm().getTitre()
                + " | Salle " + s.getSalle().getId()
                + " | " + s.getDate() + " " + s.getHeure();
    }

    private String formatPlaces(List<Integer> places) {
        if (places == null || places.isEmpty()) return "";
        return places.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private List<Integer> parsePlaces(String txt) {
        if (txt == null || txt.isBlank()) {
            throw new IllegalArgumentException("Places obligatoires (ex: 1,2,3).");
        }

        try {
            List<Integer> places = Arrays.stream(txt.split("[,;\\s]+"))
                    .filter(s -> !s.isBlank())
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .distinct()
                    .sorted()
                    .toList();

            if (places.isEmpty()) throw new IllegalArgumentException("Places invalides.");
            if (places.stream().anyMatch(p -> p <= 0))
                throw new IllegalArgumentException("Places doivent être > 0.");

            return places;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Format places invalide. Exemple: 1,2,3");
        }
    }

    private void validatePlacesAgainstSalle(Seance seance, List<Integer> places) {
        int cap = seance.getSalle().getCapacite();
        if (places.stream().anyMatch(p -> p > cap)) {
            throw new IllegalArgumentException("Certaines places dépassent la capacité de la salle (" + cap + ").");
        }
        // NOTE: l'unicité réelle est garantie par UNIQUE(seance_id, place_num) dans reservation_place
        // donc si une place est déjà prise, le DAO lèvera une exception SQL -> message affiché.
    }

    private String safeMsg(Exception e) {
        if (e.getMessage() != null && !e.getMessage().isBlank()) return e.getMessage();
        return e.getClass().getSimpleName();
    }

    private String nvl(String s) { return s == null ? "" : s; }
}
