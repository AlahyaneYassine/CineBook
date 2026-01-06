package com.cinebook.demo1.dao;

import com.cinebook.demo1.exception.DataAccessException;
import com.cinebook.demo1.model.Film;
import com.cinebook.demo1.model.Salle;
import com.cinebook.demo1.model.Seance;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class SeanceDAO {

    private final Connection conn;

    public SeanceDAO(Connection conn) {
        this.conn = conn;
    }

    // ================== CREATE ==================
    public void createSeance(Seance s) throws DataAccessException {
        String sql = "INSERT INTO seance (id, film_id, salle_id, date, heure, tarif) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getId());
            ps.setString(2, s.getFilm().getId());
            ps.setString(3, s.getSalle().getId());
            ps.setDate(4, Date.valueOf(s.getDate()));
            ps.setTime(5, Time.valueOf(s.getHeure()));
            ps.setDouble(6, s.getTarif());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Erreur création séance", e);
        }
    }

    // ================== READ BY ID (JOIN) ==================
    public Seance readSeanceById(String id) throws DataAccessException {

        String sql = """
            SELECT
                s.id AS seance_id, s.date, s.heure, s.tarif,
                f.id AS film_id, f.titre, f.genre, f.duree, f.ageRestriction,
                sa.id AS salle_id, sa.capacite, sa.type
            FROM seance s
            JOIN film f ON s.film_id = f.id
            JOIN salle sa ON s.salle_id = sa.id
            WHERE s.id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                Film film = new Film(
                        rs.getString("film_id"),
                        rs.getString("titre"),
                        rs.getString("genre"),
                        rs.getInt("duree"),
                        rs.getInt("ageRestriction")
                );

                Salle salle = new Salle(
                        rs.getString("salle_id"),
                        rs.getInt("capacite"),
                        rs.getString("type")
                );

                LocalDate date = rs.getDate("date").toLocalDate();
                LocalTime heure = rs.getTime("heure").toLocalTime();
                double tarif = rs.getDouble("tarif");

                return new Seance(rs.getString("seance_id"), film, salle, date, heure, tarif);
            }

        } catch (SQLException e) {
            throw new DataAccessException("Erreur lecture séance", e);
        }
    }

    // ✅ Alias (si ton controller fait dao.readAll())
    public List<Seance> readAll() throws DataAccessException {
        return readAllSeances();
    }

    // ================== READ ALL (JOIN) ==================
    public List<Seance> readAllSeances() throws DataAccessException {

        String sql = """
            SELECT
                s.id AS seance_id, s.date, s.heure, s.tarif,
                f.id AS film_id, f.titre, f.genre, f.duree, f.ageRestriction,
                sa.id AS salle_id, sa.capacite, sa.type
            FROM seance s
            JOIN film f ON s.film_id = f.id
            JOIN salle sa ON s.salle_id = sa.id
            ORDER BY s.date, s.heure
        """;

        List<Seance> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Film film = new Film(
                        rs.getString("film_id"),
                        rs.getString("titre"),
                        rs.getString("genre"),
                        rs.getInt("duree"),
                        rs.getInt("ageRestriction")
                );

                Salle salle = new Salle(
                        rs.getString("salle_id"),
                        rs.getInt("capacite"),
                        rs.getString("type")
                );

                Seance seance = new Seance(
                        rs.getString("seance_id"),
                        film,
                        salle,
                        rs.getDate("date").toLocalDate(),
                        rs.getTime("heure").toLocalTime(),
                        rs.getDouble("tarif")
                );

                list.add(seance);
            }

        } catch (SQLException e) {
            throw new DataAccessException("Erreur lecture toutes séances", e);
        }

        return list;
    }

    // ================== UPDATE ==================
    public void updateSeance(Seance s) throws DataAccessException {
        String sql = "UPDATE seance SET film_id=?, salle_id=?, date=?, heure=?, tarif=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getFilm().getId());
            ps.setString(2, s.getSalle().getId());
            ps.setDate(3, Date.valueOf(s.getDate()));
            ps.setTime(4, Time.valueOf(s.getHeure()));
            ps.setDouble(5, s.getTarif());
            ps.setString(6, s.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Erreur update séance", e);
        }
    }

    // ================== DELETE ==================
    public void deleteSeance(String id) throws DataAccessException {
        String sql = "DELETE FROM seance WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Erreur suppression séance", e);
        }
    }
}
