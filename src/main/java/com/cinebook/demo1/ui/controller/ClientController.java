package com.cinebook.demo1.ui.controller;

import com.cinebook.demo1.app.Session;
import com.cinebook.demo1.dao.UtilisateurDAO;
import com.cinebook.demo1.dao.DB;
import com.cinebook.demo1.dao.ReservationDAO;
import com.cinebook.demo1.dao.SeanceDAO;
import com.cinebook.demo1.model.Reservation;
import com.cinebook.demo1.model.Seance;
import com.cinebook.demo1.model.Utilisateur;
import com.cinebook.demo1.ui.navigation.Navigator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Dialog;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ClientController {

    @FXML private Label welcomeLabel;
    @FXML private Label selectedSeanceLabel;
    @FXML private Label messageLabel;

    @FXML private TableView<Seance> seancesTable;
    @FXML private TableColumn<Seance, String> colFilm;
    @FXML private TableColumn<Seance, String> colSalle;
    @FXML private TableColumn<Seance, String> colDate;
    @FXML private TableColumn<Seance, String> colHeure;
    @FXML private TableColumn<Seance, String> colTarif;
    @FXML private TableColumn<Seance, String> colPlacesDispo;

    @FXML private TextField placesField;

    @FXML
    public void initialize() {
        // ======= Session =======
        Utilisateur u = Session.getCurrentUser();
        if (u != null) {
            welcomeLabel.setText("Bienvenue " + u.getUsername() + " (" + u.getRole() + ")");
        } else {
            welcomeLabel.setText("Bienvenue");
        }

        selectedSeanceLabel.setText("(aucune)");
        messageLabel.setText("");

        // ======= Colonnes =======
        colFilm.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFilm().getTitre()));
        colSalle.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSalle().getId()));
        colDate.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getDate())));
        colHeure.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getHeure())));
        colTarif.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getTarif())));
        colPlacesDispo.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getPlacesDisponibles())));

        // ======= Sélection table =======
        seancesTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                selectedSeanceLabel.setText(formatSeance(sel));
                messageLabel.setText("");
            } else {
                selectedSeanceLabel.setText("(aucune)");
            }
        });

        // ======= Charger données =======
        loadSeances();
    }

    @FXML
    public void onDeleteAccount() {
        Utilisateur current = Session.getCurrentUser();
        if (current == null) {
            messageLabel.setText("Session expirée. Reconnecte-toi.");
            return;
        }

        // ====== 1) Popup saisie identifiant + mot de passe ======
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Suppression du compte");
        dialog.setHeaderText("⚠️ Confirme ton identité pour supprimer ton compte");

        ButtonType deleteBtn = new ButtonType("Supprimer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(deleteBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField identField = new TextField();
        identField.setPromptText("username ou email");

        PasswordField passField = new PasswordField();
        passField.setPromptText("mot de passe");

        grid.addRow(0, new Label("Username ou Email :"), identField);
        grid.addRow(1, new Label("Mot de passe :"), passField);

        dialog.getDialogPane().setContent(grid);

        // désactiver bouton supprimer tant que champs vides
        Node deleteButtonNode = dialog.getDialogPane().lookupButton(deleteBtn);
        deleteButtonNode.setDisable(true);

        identField.textProperty().addListener((o, a, b) -> {
            deleteButtonNode.setDisable(identField.getText().isBlank() || passField.getText().isBlank());
        });
        passField.textProperty().addListener((o, a, b) -> {
            deleteButtonNode.setDisable(identField.getText().isBlank() || passField.getText().isBlank());
        });

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isEmpty() || res.get() != deleteBtn) return;

        String ident = identField.getText().trim();
        String password = passField.getText();

        // ====== 2) Vérifier identifiant + mot de passe en DB ======
        try (Connection conn = DB.getConnection()) {
            UtilisateurDAO uDao = new UtilisateurDAO(conn);

            Utilisateur found;
            if (ident.contains("@")) {
                found = uDao.readUtilisateurByEmail(ident);
            } else {
                found = uDao.readUtilisateurByUsername(ident);
            }

            if (found == null) {
                messageLabel.setText("Identifiant introuvable.");
                return;
            }

            // IMPORTANT: doit être le même utilisateur que la session
            if (!found.getId().equals(current.getId())) {
                messageLabel.setText("Cet identifiant ne correspond pas au compte connecté.");
                return;
            }

            if (found.getPasswordHash() == null || !password.equals(found.getPasswordHash())) {
                messageLabel.setText("Mot de passe incorrect.");
                return;
            }

            // ====== 3) Confirmation finale ======
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Suppression du compte");
            confirm.setHeaderText("Dernière confirmation");
            confirm.setContentText(
                    "Cette action est irréversible.\n"
                            + "Toutes tes réservations seront supprimées.\n\n"
                            + "Supprimer le compte : " + current.getUsername() + " ?"
            );

            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

            // ====== 4) Supprimer réservations + utilisateur ======
            ReservationDAO rDao = new ReservationDAO(conn);
            rDao.deleteReservationsByUsername(current.getUsername());

            uDao.deleteUtilisateur(current.getId());

            // ====== 5) Logout + retour login ======
            Session.clear();
            Navigator.navigateFromNode(seancesTable, "/com/cinebook/demo1/login.fxml", "CineBook - Connexion", 520.0, 320.0);

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Erreur suppression compte : " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }



    // ✅ AJOUT: correspond à onAction="#onOpenMyReservations" dans client.fxml
    @FXML
    public void onOpenMyReservations() {
        Stage currentStage = (Stage) seancesTable.getScene().getWindow();
        Navigator.openModal("/com/cinebook/demo1/client/mesReservations.fxml",
                "CineBook - Mes Réservations", 900, 550, currentStage);
    }

    // ✅ AJOUT: correspond à onAction="#onLogout" dans client.fxml
    @FXML
    public void onLogout() {
        Session.clear();
        Navigator.navigateFromNode(seancesTable,
                "/com/cinebook/demo1/login.fxml",
                "CineBook - Connexion", 520.0, 320.0);
    }

    private String formatSeance(Seance s) {
        return s.getFilm().getTitre()
                + " | Salle " + s.getSalle().getId()
                + " | " + s.getDate() + " " + s.getHeure();
    }

    private void loadSeances() {
        try (Connection conn = DB.getConnection()) {
            SeanceDAO dao = new SeanceDAO(conn);
            List<Seance> list = dao.readAllSeances();
            seancesTable.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            messageLabel.setText("Erreur chargement séances : " + safeMsg(e));
        }
    }

    // ==========================
    //        ACTIONS UI
    // ==========================

    @FXML
    public void onRefresh() {
        loadSeances();
        messageLabel.setText("");
    }

    @FXML
    public void onClear() {
        placesField.clear();
        seancesTable.getSelectionModel().clearSelection();
        selectedSeanceLabel.setText("(aucune)");
        messageLabel.setText("");
    }

    @FXML
    public void onReserve() {
        Utilisateur user = Session.getCurrentUser();
        if (user == null) {
            messageLabel.setText("Session expirée. Reconnecte-toi.");
            return;
        }

        Seance seance = seancesTable.getSelectionModel().getSelectedItem();
        if (seance == null) {
            messageLabel.setText("Sélectionne une séance.");
            return;
        }

        final List<Integer> places;
        try {
            places = parsePlaces(placesField.getText());
        } catch (IllegalArgumentException e) {
            messageLabel.setText(e.getMessage());
            return;
        }

        // Vérifier capacité
        int cap = seance.getSalle().getCapacite();
        if (places.stream().anyMatch(p -> p > cap)) {
            messageLabel.setText("Certaines places dépassent la capacité (" + cap + ").");
            return;
        }

        // Petite vérification UX (la vraie protection = contrainte uq_seance_place en DB)
        if (places.size() > seance.getPlacesDisponibles()) {
            messageLabel.setText("Pas assez de places disponibles.");
            return;
        }

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

            messageLabel.setText("✅ Réservation confirmée !");
            onClear();
            loadSeances();

        } catch (Exception e) {
            // Cas le plus courant : place déjà prise -> contrainte uq_seance_place
            if (isDuplicateSeatError(e)) {
                messageLabel.setText("❌ Une des places est déjà réservée pour cette séance. Choisis d'autres places.");
            } else {
                messageLabel.setText("Erreur réservation : " + safeMsg(e));
            }
        }
    }

    // ==========================
    //          HELPERS
    // ==========================

    private List<Integer> parsePlaces(String txt) {
        if (txt == null || txt.isBlank()) {
            throw new IllegalArgumentException("Places obligatoires (ex: 1,2,3).");
        }

        try {
            List<Integer> places = Arrays.stream(txt.split("[,;\\s]+"))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .map(Integer::parseInt)
                    .distinct()
                    .sorted()
                    .toList();

            if (places.isEmpty()) throw new IllegalArgumentException("Places invalides.");
            if (places.stream().anyMatch(p -> p <= 0)) throw new IllegalArgumentException("Places doivent être > 0.");

            return places;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Format places invalide. Exemple: 1,2,3");
        }
    }

    private boolean isDuplicateSeatError(Exception e) {
        // MySQL duplicate key = SQLState 23000 ou message "Duplicate entry"
        Throwable t = e;
        while (t != null) {
            if (t instanceof SQLException sqlEx) {
                String state = sqlEx.getSQLState();
                if ("23000".equals(state)) return true;
            }
            String msg = t.getMessage();
            if (msg != null && msg.toLowerCase().contains("duplicate entry")) return true;
            t = t.getCause();
        }
        return false;
    }

    private String safeMsg(Exception e) {
        if (e.getMessage() != null && !e.getMessage().isBlank()) return e.getMessage();
        return e.getClass().getSimpleName();
    }
}
