package com.cinebook.demo1.dao;

import com.cinebook.demo1.exception.DataAccessException;
import com.cinebook.demo1.model.Salle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SalleDAO {

    private final Connection connection;

    public SalleDAO(Connection connection) {
        this.connection = connection;
    }

    // ================== CREATE ==================
    public void createSalle(Salle salle) throws DataAccessException {
        String sql = "INSERT INTO salle (id, capacite, type) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, salle.getId());
            stmt.setInt(2, salle.getCapacite());
            stmt.setString(3, salle.getType());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Erreur lors de la création de la salle", e);
        }
    }

    // ================== READ BY ID ==================
    public Salle readSalleById(String id) throws DataAccessException {
        String sql = "SELECT * FROM salle WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Salle(
                            rs.getString("id"),
                            rs.getInt("capacite"),
                            rs.getString("type")
                    );
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Erreur lors de la lecture de la salle", e);
        }
    }

    // ================== READ ALL ==================
    public List<Salle> readAllSalles() throws DataAccessException {
        String sql = "SELECT * FROM salle";
        List<Salle> salles = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                salles.add(new Salle(
                        rs.getString("id"),
                        rs.getInt("capacite"),
                        rs.getString("type")
                ));
            }
            return salles;

        } catch (SQLException e) {
            throw new DataAccessException("Erreur lors de la lecture des salles", e);
        }
    }

    // ================== UPDATE ==================
    public void updateSalle(Salle salle) throws DataAccessException {
        String sql = "UPDATE salle SET capacite = ?, type = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, salle.getCapacite());
            stmt.setString(2, salle.getType());
            stmt.setString(3, salle.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Erreur lors de la mise à jour de la salle", e);
        }
    }

    // ================== DELETE ==================
    public void deleteSalle(String id) throws DataAccessException {
        String sql = "DELETE FROM salle WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Erreur lors de la suppression de la salle", e);
        }
    }
}
