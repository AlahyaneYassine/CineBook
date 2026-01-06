package com.cinebook.demo1.ui.controller;

import com.cinebook.demo1.app.Session;
import com.cinebook.demo1.dao.DB;
import com.cinebook.demo1.dao.ReservationDAO;
import com.cinebook.demo1.model.Reservation;
import com.cinebook.demo1.model.Seance;
import com.cinebook.demo1.model.Utilisateur;
import com.cinebook.demo1.ui.navigation.Navigator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class MyReservationsController {

    @FXML private TableView<Reservation> table;

    @FXML private TableColumn<Reservation, String> colFilm;
    @FXML private TableColumn<Reservation, String> colSalle;
    @FXML private TableColumn<Reservation, String> colDate;
    @FXML private TableColumn<Reservation, String> colHeure;
    @FXML private TableColumn<Reservation, String> colPlaces;
    @FXML private TableColumn<Reservation, String> colDateRes;

    @FXML private Label messageLabel;

    private static final String FXML_SEANCES = "/com/cinebook/demo1/client/client.fxml";

    private final DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        if (messageLabel != null) messageLabel.setText("");

        colFilm.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getSeance().getFilm().getTitre())
        );

        colSalle.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(d.getValue().getSeance().getSalle().getId()))
        );

        colDate.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(d.getValue().getSeance().getDate()))
        );

        colHeure.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(d.getValue().getSeance().getHeure()))
        );

        colPlaces.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getPlaces() == null ? "" :
                                d.getValue().getPlaces().stream()
                                        .map(String::valueOf)
                                        .collect(Collectors.joining(","))
                )
        );

        colDateRes.setCellValueFactory(d -> {
            LocalDateTime dt = d.getValue().getDateReservation();
            return new SimpleStringProperty(dt == null ? "" : dt.format(dtFmt));
        });

        load();
    }

    private void load() {
        Utilisateur u = Session.getCurrentUser();
        if (u == null) {
            messageLabel.setText("Session expirée. Reconnecte-toi.");
            table.setItems(FXCollections.observableArrayList());
            return;
        }

        try (Connection conn = DB.getConnection()) {
            ReservationDAO dao = new ReservationDAO(conn);
            List<Reservation> list = dao.readReservationsByUsername(u.getUsername());
            table.setItems(FXCollections.observableArrayList(list));
            messageLabel.setText(list.isEmpty() ? "Aucune réservation." : "");
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Erreur chargement : " + safeMsg(e));
        }
    }

    @FXML
    public void onRefresh() {
        load();
    }

    @FXML
    public void onCancel() {
        Utilisateur current = Session.getCurrentUser();
        if (current == null) {
            messageLabel.setText("Session expirée. Reconnecte-toi.");
            return;
        }

        Reservation selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Sélectionne une réservation à annuler.");
            return;
        }

        if (selected.getUser() == null || !current.getUsername().equals(selected.getUser().getUsername())) {
            messageLabel.setText("Action refusée : cette réservation ne t'appartient pas.");
            return;
        }

        Seance s = selected.getSeance();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Annulation");
        confirm.setHeaderText("Annuler cette réservation ?");
        confirm.setContentText(
                "Film: " + s.getFilm().getTitre()
                        + "\nSalle: " + s.getSalle().getId()
                        + "\nDate: " + s.getDate() + " " + s.getHeure()
                        + "\nPlaces: " + selected.getPlaces()
        );

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try (Connection conn = DB.getConnection()) {
            ReservationDAO dao = new ReservationDAO(conn);
            dao.deleteReservation(selected.getId());

            load();
            messageLabel.setText("✅ Réservation annulée.");
        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Erreur annulation : " + safeMsg(e));
        }
    }

    /**
     * ✅ Retour = navigation interne (PAS de Stage.close)
     */
    @FXML
    public void onBack() {
        Navigator.loadContent("/com/cinebook/demo1/client/client.fxml",
                com.cinebook.demo1.ui.navigation.ShellRouter.getClientContentPane());
    }


    private String safeMsg(Exception e) {
        if (e.getMessage() != null && !e.getMessage().isBlank()) return e.getMessage();
        return e.getClass().getSimpleName();
    }
}
