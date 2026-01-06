package com.cinebook.demo1.app;

import com.cinebook.demo1.dao.DB;
import com.cinebook.demo1.dao.FilmDAO;
import com.cinebook.demo1.dao.SalleDAO;
import com.cinebook.demo1.dao.SeanceDAO;
import com.cinebook.demo1.exception.DataAccessException;
import com.cinebook.demo1.model.Film;
import com.cinebook.demo1.model.Salle;
import com.cinebook.demo1.model.Seance;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Scanner;

public class InteractiveSeance {

    public static void main(String[] args) {

        try (Connection conn = DB.getConnection();
             Scanner sc = new Scanner(System.in)) {

            SeanceDAO seanceDAO = new SeanceDAO(conn);
            FilmDAO filmDAO = new FilmDAO(conn);
            SalleDAO salleDAO = new SalleDAO(conn);

            boolean running = true;

            while (running) {
                System.out.println("\n=== PANEL SEANCES ===");
                System.out.println("1. Ajouter une séance");
                System.out.println("2. Voir toutes les séances");
                System.out.println("3. Voir séance par ID");
                System.out.println("4. Modifier une séance");
                System.out.println("5. Supprimer une séance");
                System.out.println("6. Quitter");
                System.out.print("Choix : ");

                String choix = sc.nextLine();

                switch (choix) {

                    case "1": // CREATE
                        try {
                            System.out.println("\n--- Films disponibles ---");
                            List<Film> films = filmDAO.readAllFilms();
                            films.forEach(f -> System.out.println(f.getId() + " : " + f.getTitre()));

                            System.out.print("Film ID : ");
                            String filmId = sc.nextLine();
                            Film film = filmDAO.readFilmById(filmId);
                            if (film == null) {
                                System.out.println("❌ Film introuvable.");
                                break;
                            }

                            System.out.println("\n--- Salles disponibles ---");
                            List<Salle> salles = salleDAO.readAllSalles();
                            salles.forEach(s -> System.out.println(
                                    s.getId() + " : " + s.getType() + " (cap=" + s.getCapacite() + ")"
                            ));

                            System.out.print("Salle ID : ");
                            String salleId = sc.nextLine();
                            Salle salle = salleDAO.readSalleById(salleId);
                            if (salle == null) {
                                System.out.println("❌ Salle introuvable.");
                                break;
                            }

                            System.out.print("ID séance : ");
                            String seanceId = sc.nextLine();

                            System.out.print("Date (YYYY-MM-DD) : ");
                            LocalDate date = LocalDate.parse(sc.nextLine());

                            System.out.print("Heure (HH:MM) : ");
                            LocalTime heure = LocalTime.parse(sc.nextLine());

                            System.out.print("Tarif : ");
                            double tarif = Double.parseDouble(sc.nextLine());

                            Seance newSeance = new Seance(seanceId, film, salle, date, heure, tarif);
                            seanceDAO.createSeance(newSeance);

                            System.out.println("✅ Séance ajoutée : " + newSeance);

                        } catch (Exception ex) {
                            System.out.println("❌ Erreur création séance : " + ex.getMessage());
                        }
                        break;

                    case "2": // READ ALL
                        try {
                            List<Seance> seances = seanceDAO.readAllSeances();
                            System.out.println("\n=== LISTE DES SEANCES ===");
                            if (seances.isEmpty()) {
                                System.out.println("Aucune séance trouvée.");
                            } else {
                                seances.forEach(System.out::println);
                            }
                        } catch (DataAccessException e) {
                            System.out.println("❌ Erreur lecture séances : " + e.getMessage());
                        }
                        break;

                    case "3": // READ BY ID
                        System.out.print("ID séance : ");
                        String idRead = sc.nextLine();
                        try {
                            Seance s = seanceDAO.readSeanceById(idRead);
                            if (s == null) {
                                System.out.println("❌ Séance introuvable");
                            } else {
                                System.out.println("✅ Séance : " + s);
                            }
                        } catch (DataAccessException e) {
                            System.out.println("❌ Erreur lecture séance : " + e.getMessage());
                        }
                        break;

                    case "4": // UPDATE
                        System.out.print("ID séance à modifier : ");
                        String idUpdate = sc.nextLine();

                        try {
                            Seance exist = seanceDAO.readSeanceById(idUpdate);
                            if (exist == null) {
                                System.out.println("❌ Séance introuvable.");
                                break;
                            }

                            System.out.println("Séance actuelle : " + exist);

                            System.out.print("Nouvelle date (YYYY-MM-DD) : ");
                            LocalDate newDate = LocalDate.parse(sc.nextLine());

                            System.out.print("Nouvelle heure (HH:MM) : ");
                            LocalTime newHeure = LocalTime.parse(sc.nextLine());

                            System.out.print("Nouveau tarif : ");
                            double newTarif = Double.parseDouble(sc.nextLine());

                            Seance updated = new Seance(
                                    exist.getId(),
                                    exist.getFilm(),
                                    exist.getSalle(),
                                    newDate,
                                    newHeure,
                                    newTarif
                            );

                            seanceDAO.updateSeance(updated);
                            System.out.println("✅ Séance modifiée : " + updated);

                        } catch (Exception e) {
                            System.out.println("❌ Erreur update : " + e.getMessage());
                        }
                        break;

                    case "5": // DELETE
                        System.out.print("ID séance à supprimer : ");
                        String idDel = sc.nextLine();

                        try {
                            seanceDAO.deleteSeance(idDel);
                            System.out.println("✅ Séance supprimée !");
                        } catch (DataAccessException e) {
                            System.out.println("❌ Erreur suppression : " + e.getMessage());
                        }
                        break;

                    case "6":
                        running = false;
                        System.out.println("👋 Fermeture panel séances");
                        break;

                    default:
                        System.out.println("Choix invalide !");
                }
            }

        } catch (Exception e) {
            System.out.println("❌ Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
