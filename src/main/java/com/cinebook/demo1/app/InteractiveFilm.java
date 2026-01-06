package com.cinebook.demo1.app;

import com.cinebook.demo1.dao.FilmDAO;
import com.cinebook.demo1.exception.DataAccessException;
import com.cinebook.demo1.model.Film;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class InteractiveFilm {

    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/projet_java_db?useSSL=false&serverTimezone=UTC";
        String dbUser = "root";
        String dbPassword = "root";

        try (Connection conn = DriverManager.getConnection(url, dbUser, dbPassword);
             Scanner sc = new Scanner(System.in)) {

            FilmDAO filmDAO = new FilmDAO(conn);

            // ================== Connexion Admin ==================
            System.out.println("=== Connexion ADMIN pour gestion des films ===");
            System.out.print("Username : ");
            String username = sc.nextLine();
            System.out.print("Mot de passe : ");
            String password = sc.nextLine();

            // TODO: vérifier admin dans la table utilisateur
            System.out.println("Connexion réussie ! Bienvenue, " + username);

            boolean running = true;
            while (running) {
                System.out.println("\n=== PANEL FILMS ===");
                System.out.println("1. Ajouter un film");
                System.out.println("2. Voir tous les films");
                System.out.println("3. Voir film par ID");
                System.out.println("4. Modifier un film");
                System.out.println("5. Supprimer un film");
                System.out.println("6. Quitter");
                System.out.print("Choix : ");

                String choix = sc.nextLine();

                switch (choix) {

                    case "1": // CREATE
                        System.out.print("Titre : ");
                        String titre = sc.nextLine();
                        System.out.print("Genre : ");
                        String genre = sc.nextLine();
                        System.out.print("Durée (minutes) : ");
                        int duree = Integer.parseInt(sc.nextLine());
                        System.out.print("Age minimum : ");
                        int ageMin = Integer.parseInt(sc.nextLine());

                        Film newFilm = new Film(UUID.randomUUID().toString(), titre, genre, duree, ageMin);
                        try {
                            filmDAO.createFilm(newFilm);
                            System.out.println("Film ajouté : " + newFilm.getTitre());
                        } catch (DataAccessException e) {
                            System.out.println("Erreur création film : " + e.getMessage());
                        }
                        break;

                    case "2": // READ ALL
                        try {
                            List<Film> films = filmDAO.readAllFilms();
                            System.out.println("\n=== Liste des films ===");
                            if (films.isEmpty()) {
                                System.out.println("Aucun film trouvé.");
                            } else {
                                films.forEach(f -> System.out.println(
                                        f.getId() + " : " + f.getTitre() + " (" + f.getGenre() + ", " +
                                                f.getDuree() + " min, Age " + f.getAgeRestriction() + "+)"
                                ));
                            }
                        } catch (DataAccessException e) {
                            System.out.println("Erreur lecture films : " + e.getMessage());
                        }
                        break;

                    case "3": // READ BY ID
                        System.out.print("ID du film : ");
                        String idRead = sc.nextLine();
                        try {
                            Film film = filmDAO.readFilmById(idRead);
                            if (film == null) {
                                System.out.println("Film introuvable");
                            } else {
                                System.out.println("Film : " + film.getTitre() + " (" + film.getGenre() + ", " +
                                        film.getDuree() + " min, Age " + film.getAgeRestriction() + "+)");
                            }
                        } catch (DataAccessException e) {
                            System.out.println("Erreur lecture film : " + e.getMessage());
                        }
                        break;

                    case "4": // UPDATE
                        System.out.print("ID du film à modifier : ");
                        String idUpdate = sc.nextLine();
                        try {
                            Film exist = filmDAO.readFilmById(idUpdate);
                            if (exist == null) {
                                System.out.println("❌ Film introuvable");
                                break;
                            }

                            System.out.print("Nouveau titre (laisser vide pour garder actuel) : ");
                            String newTitre = sc.nextLine();
                            System.out.print("Nouveau genre : ");
                            String newGenre = sc.nextLine();
                            System.out.print("Nouvelle durée : ");
                            String durInput = sc.nextLine();
                            System.out.print("Nouvel âge minimum : ");
                            String ageInput = sc.nextLine();

                            if (!newTitre.isBlank()) exist.setTitre(newTitre);
                            if (!newGenre.isBlank()) exist.setGenre(newGenre);
                            if (!durInput.isBlank()) exist.setDuree(Integer.parseInt(durInput));
                            if (!ageInput.isBlank()) exist.setAgeRestriction(Integer.parseInt(ageInput));

                            filmDAO.updateFilm(exist);
                            System.out.println("Film mis à jour : " + exist.getTitre());

                        } catch (DataAccessException e) {
                            System.out.println("Erreur mise à jour film : " + e.getMessage());
                        }
                        break;

                    case "5": // DELETE
                        System.out.print("ID du film à supprimer : ");
                        String idDel = sc.nextLine();
                        try {
                            filmDAO.deleteFilm(idDel);
                            System.out.println("Film supprimé !");
                        } catch (DataAccessException e) {
                            System.out.println("Erreur suppression film : " + e.getMessage());
                        }
                        break;

                    case "6":
                        running = false;
                        System.out.println("Fermeture du panel films");
                        break;

                    default:
                        System.out.println("Choix invalide !");
                }
            }

        } catch (SQLException e) {
            System.out.println("Erreur connexion DB : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
