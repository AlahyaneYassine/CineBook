package com.cinebook.demo1.service;

import com.cinebook.demo1.dao.FilmDAO;
import com.cinebook.demo1.exception.DataAccessException;
import com.cinebook.demo1.model.Film;
import com.cinebook.demo1.model.Utilisateur;

import java.util.List;

public class FilmService {

    private final FilmDAO filmDAO;

    public FilmService(FilmDAO filmDAO) {
        this.filmDAO = filmDAO;
    }

    // ================== READ ==================
    public List<Film> getAllFilms() throws DataAccessException {
        return filmDAO.readAllFilms();
    }

    public Film getFilmById(String id) throws DataAccessException {
        return filmDAO.readFilmById(id);
    }

    // ================== CREATE ==================
    public void addFilm(Film film, Utilisateur user) throws DataAccessException {
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new SecurityException("Seul un ADMIN peut ajouter un film !");
        }
        filmDAO.createFilm(film);
    }

    // ================== UPDATE ==================
    public void updateFilm(Film film, Utilisateur user) throws DataAccessException {
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new SecurityException("Seul un ADMIN peut modifier un film !");
        }
        filmDAO.updateFilm(film);
    }

    // ================== DELETE ==================
    public void deleteFilm(String filmId, Utilisateur user) throws DataAccessException {
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new SecurityException("Seul un ADMIN peut supprimer un film !");
        }
        filmDAO.deleteFilm(filmId);
    }
}
