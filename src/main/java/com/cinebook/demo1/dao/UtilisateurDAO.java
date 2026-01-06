package com.cinebook.demo1.dao;

import com.cinebook.demo1.exception.DataAccessException;
import com.cinebook.demo1.model.Utilisateur;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDAO {

    private final Connection connection;

    public UtilisateurDAO(Connection connection) {
        this.connection = connection;
    }

    // ================== CREATE ==================
    public void createUtilisateur(Utilisateur utilisateur) throws DataAccessException {
        String sql = "INSERT INTO utilisateur (id, username, passwordHash, role, nom, prenom, email, lastProfileUpdate) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, utilisateur.getId());
            stmt.setString(2, utilisateur.getUsername());
            stmt.setString(3, utilisateur.getPasswordHash());
            stmt.setString(4, utilisateur.getRole());
            stmt.setString(5, utilisateur.getNom());
            stmt.setString(6, utilisateur.getPrenom());
            stmt.setString(7, utilisateur.getEmail());
            stmt.setDate(8,
                    utilisateur.getLastProfileUpdate() != null
                            ? Date.valueOf(utilisateur.getLastProfileUpdate())
                            : null
            );
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Erreur lors de la création de l'utilisateur", e);
        }
    }

    public Utilisateur readUtilisateurByEmail(String email) throws DataAccessException {
        String sql = "SELECT * FROM utilisateur WHERE email = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    LocalDate lastUpdate =
                            rs.getDate("lastProfileUpdate") != null
                                    ? rs.getDate("lastProfileUpdate").toLocalDate()
                                    : null;

                    return new Utilisateur(
                            rs.getString("id"),
                            rs.getString("username"),
                            rs.getString("passwordHash"),
                            rs.getString("role"),
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getString("email"),
                            lastUpdate
                    );
                }
                return null;
            }

        } catch (SQLException e) {
            throw new DataAccessException("Erreur lors de la lecture de l'utilisateur par email", e);
        }
    }


    // ================== READ BY ID ==================
    public Utilisateur readUtilisateurById(String id) throws DataAccessException {
        String sql = "SELECT * FROM utilisateur WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    LocalDate lastUpdate =
                            rs.getDate("lastProfileUpdate") != null
                                    ? rs.getDate("lastProfileUpdate").toLocalDate()
                                    : null;

                    return new Utilisateur(
                            rs.getString("id"),
                            rs.getString("username"),
                            rs.getString("passwordHash"),
                            rs.getString("role"),
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getString("email"),
                            lastUpdate
                    );
                }
                return null;
            }

        } catch (SQLException e) {
            throw new DataAccessException("Erreur lors de la lecture de l'utilisateur", e);
        }
    }

    // ================== READ BY USERNAME ==================
    public Utilisateur readUtilisateurByUsername(String username) throws DataAccessException {
        String sql = "SELECT * FROM utilisateur WHERE username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    LocalDate lastUpdate =
                            rs.getDate("lastProfileUpdate") != null
                                    ? rs.getDate("lastProfileUpdate").toLocalDate()
                                    : null;

                    return new Utilisateur(
                            rs.getString("id"),
                            rs.getString("username"),
                            rs.getString("passwordHash"),
                            rs.getString("role"),
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getString("email"),
                            lastUpdate
                    );
                }
                return null;
            }

        } catch (SQLException e) {
            throw new DataAccessException(
                    "Erreur lors de la lecture de l'utilisateur par username", e
            );
        }
    }

    // ================== READ ALL ==================
    public List<Utilisateur> readAllUtilisateurs() throws DataAccessException {
        String sql = "SELECT * FROM utilisateur";
        List<Utilisateur> utilisateurs = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                LocalDate lastUpdate =
                        rs.getDate("lastProfileUpdate") != null
                                ? rs.getDate("lastProfileUpdate").toLocalDate()
                                : null;

                utilisateurs.add(new Utilisateur(
                        rs.getString("id"),
                        rs.getString("username"),
                        rs.getString("passwordHash"),
                        rs.getString("role"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        lastUpdate
                ));
            }
            return utilisateurs;

        } catch (SQLException e) {
            throw new DataAccessException("Erreur lors de la lecture des utilisateurs", e);
        }
    }

    // ================== UPDATE ==================
    public void updateUtilisateur(Utilisateur utilisateur) throws DataAccessException {
        String sql = "UPDATE utilisateur SET passwordHash = ?, role = ?, " +
                "nom = ?, prenom = ?, email = ?, lastProfileUpdate = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, utilisateur.getPasswordHash());
            stmt.setString(2, utilisateur.getRole());
            stmt.setString(3, utilisateur.getNom());
            stmt.setString(4, utilisateur.getPrenom());
            stmt.setString(5, utilisateur.getEmail());
            stmt.setDate(6,
                    utilisateur.getLastProfileUpdate() != null
                            ? Date.valueOf(utilisateur.getLastProfileUpdate())
                            : null
            );
            stmt.setString(7, utilisateur.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Erreur lors de la mise à jour de l'utilisateur", e);
        }
    }

    // ================== DELETE ==================
    public void deleteUtilisateur(String id) throws DataAccessException {
        String sql = "DELETE FROM utilisateur WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Erreur lors de la suppression de l'utilisateur", e);
        }
    }
}
