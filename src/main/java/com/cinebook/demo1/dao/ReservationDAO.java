package com.cinebook.demo1.dao;

import com.cinebook.demo1.exception.DataAccessException;
import com.cinebook.demo1.model.Reservation;
import com.cinebook.demo1.model.Seance;
import com.cinebook.demo1.model.Utilisateur;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    private final Connection conn;
    private final UtilisateurDAO utilisateurDAO;
    private final SeanceDAO seanceDAO;

    public ReservationDAO(Connection conn) {
        this.conn = conn;
        this.utilisateurDAO = new UtilisateurDAO(conn);
        this.seanceDAO = new SeanceDAO(conn);
    }

    public void deleteReservationsByUsername(String username) throws DataAccessException {
        // IMPORTANT: reservation_place est supprimée via FK ON DELETE CASCADE sur reservation_id
        final String sql = "DELETE FROM reservation WHERE user_username = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Erreur suppression réservations utilisateur", e);
        }
    }


    // ================== CREATE ==================
    public void createReservation(Reservation r) throws DataAccessException {
        final String insertRes =
                "INSERT INTO reservation (id, user_username, seance_id, date_reservation) VALUES (?, ?, ?, ?)";
        final String insertPlace =
                "INSERT INTO reservation_place (reservation_id, seance_id, place_num) VALUES (?, ?, ?)";

        boolean oldAutoCommit;
        try {
            oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try (PreparedStatement psRes = conn.prepareStatement(insertRes);
                 PreparedStatement psPlace = conn.prepareStatement(insertPlace)) {

                psRes.setString(1, r.getId());
                psRes.setString(2, r.getUser().getUsername());
                psRes.setString(3, r.getSeance().getId());

                // si null => MySQL met CURRENT_TIMESTAMP (car colonne a DEFAULT)
                if (r.getDateReservation() == null) {
                    psRes.setNull(4, Types.TIMESTAMP);
                } else {
                    psRes.setTimestamp(4, Timestamp.valueOf(r.getDateReservation()));
                }

                psRes.executeUpdate();

                for (Integer place : r.getPlaces()) {
                    psPlace.setString(1, r.getId());
                    psPlace.setString(2, r.getSeance().getId());
                    psPlace.setInt(3, place);
                    psPlace.addBatch();
                }
                psPlace.executeBatch();

                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                throw new DataAccessException("Erreur création réservation (rollback)", e);
            } finally {
                conn.setAutoCommit(oldAutoCommit);
            }

        } catch (SQLException e) {
            throw new DataAccessException("Erreur création réservation", e);
        }
    }

    // ================== READ BY ID ==================
    public Reservation readReservationById(String id) throws DataAccessException {
        final String sql = "SELECT id, user_username, seance_id, date_reservation FROM reservation WHERE id=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                String userUsername = rs.getString("user_username");
                String seanceId = rs.getString("seance_id");

                Timestamp ts = rs.getTimestamp("date_reservation");
                LocalDateTime dateRes = (ts != null) ? ts.toLocalDateTime() : null;

                Utilisateur user = utilisateurDAO.readUtilisateurByUsername(userUsername);
                Seance seance = seanceDAO.readSeanceById(seanceId);

                if (user == null) throw new DataAccessException("Utilisateur introuvable : " + userUsername);
                if (seance == null) throw new DataAccessException("Séance introuvable : " + seanceId);

                List<Integer> places = readPlacesForReservation(id);
                return new Reservation(id, user, seance, places, dateRes);
            }

        } catch (SQLException e) {
            throw new DataAccessException("Erreur lecture réservation", e);
        }
    }

    // ================== READ ALL ==================
    public List<Reservation> readAllReservations() throws DataAccessException {
        final String sql = "SELECT id, user_username, seance_id, date_reservation FROM reservation ORDER BY date_reservation DESC";
        List<Reservation> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String id = rs.getString("id");
                String userUsername = rs.getString("user_username");
                String seanceId = rs.getString("seance_id");

                Timestamp ts = rs.getTimestamp("date_reservation");
                LocalDateTime dateRes = (ts != null) ? ts.toLocalDateTime() : null;

                Utilisateur user = utilisateurDAO.readUtilisateurByUsername(userUsername);
                Seance seance = seanceDAO.readSeanceById(seanceId);

                // si FK cassée, on ignore proprement
                if (user == null || seance == null) continue;

                List<Integer> places = readPlacesForReservation(id);
                list.add(new Reservation(id, user, seance, places, dateRes));
            }

            return list;

        } catch (SQLException e) {
            throw new DataAccessException("Erreur lecture toutes réservations", e);
        }
    }

    // ================== READ BY USERNAME ==================
    public List<Reservation> readReservationsByUsername(String username) throws DataAccessException {
        final String sql = """
                SELECT id, user_username, seance_id, date_reservation
                FROM reservation
                WHERE user_username = ?
                ORDER BY date_reservation DESC
                """;

        List<Reservation> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("id");
                    String userUsername = rs.getString("user_username");
                    String seanceId = rs.getString("seance_id");

                    Timestamp ts = rs.getTimestamp("date_reservation");
                    LocalDateTime dateRes = (ts != null) ? ts.toLocalDateTime() : null;

                    Utilisateur user = utilisateurDAO.readUtilisateurByUsername(userUsername);
                    Seance seance = seanceDAO.readSeanceById(seanceId);
                    if (user == null || seance == null) continue;

                    List<Integer> places = readPlacesForReservation(id);
                    list.add(new Reservation(id, user, seance, places, dateRes));
                }
            }

            return list;

        } catch (SQLException e) {
            throw new DataAccessException("Erreur lecture réservations utilisateur", e);
        }
    }

    // ================== UPDATE ==================
    // stratégie : update reservation + replace all places
    public void updateReservation(Reservation r) throws DataAccessException {
        final String updateRes = "UPDATE reservation SET user_username=?, seance_id=? WHERE id=?";
        final String deletePlaces = "DELETE FROM reservation_place WHERE reservation_id=?";
        final String insertPlace = "INSERT INTO reservation_place (reservation_id, seance_id, place_num) VALUES (?, ?, ?)";

        boolean oldAutoCommit;
        try {
            oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try (PreparedStatement psUp = conn.prepareStatement(updateRes);
                 PreparedStatement psDel = conn.prepareStatement(deletePlaces);
                 PreparedStatement psIns = conn.prepareStatement(insertPlace)) {

                psUp.setString(1, r.getUser().getUsername());
                psUp.setString(2, r.getSeance().getId());
                psUp.setString(3, r.getId());
                psUp.executeUpdate();

                psDel.setString(1, r.getId());
                psDel.executeUpdate();

                for (Integer place : r.getPlaces()) {
                    psIns.setString(1, r.getId());
                    psIns.setString(2, r.getSeance().getId());
                    psIns.setInt(3, place);
                    psIns.addBatch();
                }
                psIns.executeBatch();

                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                throw new DataAccessException("Erreur update réservation (rollback)", e);
            } finally {
                conn.setAutoCommit(oldAutoCommit);
            }

        } catch (SQLException e) {
            throw new DataAccessException("Erreur update réservation", e);
        }
    }

    // ================== DELETE ==================
    public void deleteReservation(String id) throws DataAccessException {
        final String sql = "DELETE FROM reservation WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Erreur suppression réservation", e);
        }
    }

    // lisible (pour MyReservationsController)
    public void deleteReservationWithPlaces(String id) throws DataAccessException {
        // reservation_place supprimé automatiquement grâce au ON DELETE CASCADE
        deleteReservation(id);
    }

    // ================== HELPERS ==================
    private List<Integer> readPlacesForReservation(String reservationId) throws SQLException {
        final String sql = "SELECT place_num FROM reservation_place WHERE reservation_id=? ORDER BY place_num";
        List<Integer> places = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    places.add(rs.getInt("place_num"));
                }
            }
        }

        return places;
    }
}
