package com.cinebook.demo1.dao;

import com.cinebook.demo1.exception.DataAccessException;
import com.cinebook.demo1.model.Film;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FilmDAO {

    private final Connection connection;

    public FilmDAO(Connection connection) {
        this.connection = connection;
    }

    // ================== CREATE ==================
    public void createFilm(Film film) throws DataAccessException {
        String sql = "INSERT INTO film (id, titre, genre, duree, ageRestriction) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, film.getId());
            stmt.setString(2, film.getTitre());
            stmt.setString(3, film.getGenre());
            stmt.setInt(4, film.getDuree());
            stmt.setInt(5, film.getAgeRestriction());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Erreur lors de la création du film", e);
        }
    }

    // ================== READ BY ID ==================
    public Film readFilmById(String id) throws DataAccessException {
        String sql = "SELECT * FROM film WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Film(
                            rs.getString("id"),
                            rs.getString("titre"),
                            rs.getString("genre"),
                            rs.getInt("duree"),
                            rs.getInt("ageRestriction")
                    );
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Erreur lors de la lecture du film", e);
        }
    }

    // ================== READ ALL ==================
    public List<Film> readAllFilms() throws DataAccessException {
        String sql = "SELECT * FROM film";
        List<Film> films = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Film film = new Film(
                        rs.getString("id"),
                        rs.getString("titre"),
                        rs.getString("genre"),
                        rs.getInt("duree"),
                        rs.getInt("ageRestriction")
                );
                films.add(film);
            }
            return films;

        } catch (SQLException e) {
            throw new DataAccessException("Erreur lors de la lecture des films", e);
        }
    }

    // ================== UPDATE ==================
    public void updateFilm(Film film) throws DataAccessException {
        String sql = "UPDATE film SET titre = ?, genre = ?, duree = ?, ageRestriction = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, film.getTitre());
            stmt.setString(2, film.getGenre());
            stmt.setInt(3, film.getDuree());
            stmt.setInt(4, film.getAgeRestriction());
            stmt.setString(5, film.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Erreur lors de la mise à jour du film", e);
        }
    }

    // ================== DELETE ==================
    public void deleteFilm(String id) throws DataAccessException {
        String sql = "DELETE FROM film WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Erreur lors de la suppression du film", e);
        }
    }
}
