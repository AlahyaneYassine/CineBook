package com.cinebook.demo1.app;

import com.cinebook.demo1.dao.CsvDataManager;
import com.cinebook.demo1.dao.UtilisateurDAO;
import com.cinebook.demo1.exception.DataAccessException;
import com.cinebook.demo1.model.Film;
import com.cinebook.demo1.model.Reservation;
import com.cinebook.demo1.model.Salle;
import com.cinebook.demo1.model.Seance;
import com.cinebook.demo1.model.Utilisateur;
import com.cinebook.demo1.service.ReservationService;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AppMain {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        ExecutorService ioExecutor = Executors.newFixedThreadPool(4);

        Path base = Path.of("data");
        CsvDataManager csvManager = new CsvDataManager(base, ioExecutor);

        List<Film> films;
        List<Salle> salles;
        List<Seance> seances;

        try {
            films = csvManager.loadFilmsAsync().get(3, TimeUnit.SECONDS);
            salles = csvManager.loadSallesAsync().get(3, TimeUnit.SECONDS);

            if (films.isEmpty() || salles.isEmpty()) {
                throw new RuntimeException("CSV vides");
            }

            Map<String, Film> filmMap = films.stream()
                    .collect(Collectors.toMap(Film::getId, f -> f));

            Map<String, Salle> salleMap = salles.stream()
                    .collect(Collectors.toMap(Salle::getId, s -> s));

            seances = csvManager
                    .loadSeancesAsync(filmMap, salleMap)
                    .get(3, TimeUnit.SECONDS);

        } catch (Exception e) {
            System.out.println("Chargement CSV échoué, création de données d'exemple.");
            films = List.of(
                    new Film("F1", "The Great Adventure", "Aventure", 120, 12),
                    new Film("F2", "Action Blast", "Action", 95, 16)
            );
            salles = List.of(
                    new Salle("S1", 10, "2D"),
                    new Salle("S2", 20, "IMAX")
            );
            seances = List.of(
                    new Seance("SE1", films.get(0), salles.get(0), LocalDate.now(), LocalTime.of(18, 0), 50.0),
                    new Seance("SE2", films.get(1), salles.get(1), LocalDate.now(), LocalTime.of(20, 0), 70.0)
            );
        }

        ReservationService reservationService = new ReservationService(seances);

        // Connexion MySQL et DAO
        String url = "jdbc:mysql://localhost:3306/projet_java_db?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "root";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            UtilisateurDAO utilisateurDAO = new UtilisateurDAO(conn);
            boolean appOn = true;

            while (appOn) {
                System.out.println("\n=== Bienvenue à CineBook ===");
                System.out.println("1. Se connecter");
                System.out.println("2. S'inscrire");
                System.out.println("3. Quitter");
                System.out.print("Choix : ");
                String choix = sc.nextLine();
                Utilisateur currentUser = null;

                switch (choix) {
                    case "1": // Connexion
                        System.out.print("Entrez votre username : ");
                        String uname = sc.nextLine();
                        currentUser = utilisateurDAO.readUtilisateurByUsername(uname);

                        if (currentUser == null) {
                            System.out.println("Utilisateur non trouvé !");
                            continue;
                        }

                        System.out.print("Entrez votre mot de passe : ");
                        String pass = sc.nextLine();
                        if (!pass.equals(currentUser.getPasswordHash())) {
                            System.out.println("Mot de passe incorrect !");
                            continue;
                        }

                        System.out.println("Connexion réussie. Bienvenue, " + currentUser.getUsername());
                        break;

                    case "2": // Inscription
                        System.out.print("Prénom : ");
                        String prenom = sc.nextLine();
                        System.out.print("Nom : ");
                        String nom = sc.nextLine();
                        System.out.print("Username : ");
                        String username = sc.nextLine();
                        System.out.print("Email : ");
                        String email = sc.nextLine();

                        if (utilisateurDAO.readUtilisateurByUsername(username) != null) {
                            System.out.println("Username déjà utilisé !");
                            continue;
                        }

                        System.out.print("Mot de passe : ");
                        String passwordUser = sc.nextLine();

                        currentUser = new Utilisateur(
                                UUID.randomUUID().toString(),
                                username,
                                passwordUser,
                                "CLIENT",
                                nom,
                                prenom,
                                email,
                                LocalDate.now()
                        );
                        utilisateurDAO.createUtilisateur(currentUser);
                        System.out.println("✅ Inscription réussie !");
                        break;

                    case "3":
                        appOn = false;
                        continue;

                    default:
                        System.out.println("Choix invalide !");
                        continue;
                }

                final Utilisateur userFinal = currentUser;
                boolean sessionOn = true;

                while (sessionOn) {
                    System.out.println("\n=== Menu Utilisateur ===");
                    System.out.println("1. Voir films");
                    System.out.println("2. Voir séances");
                    System.out.println("3. Réserver");
                    System.out.println("4. Annuler réservation");
                    System.out.println("5. Mes réservations");
                    System.out.println("6. Modifier profil");
                    System.out.println("7. Supprimer compte");
                    System.out.println("8. Déconnexion");
                    System.out.print("Choix : ");
                    String action = sc.nextLine();

                    switch (action) {
                        case "1":
                            System.out.println("\n=== Films disponibles ===");
                            films.forEach(f -> System.out.println(f.getId() + " : " + f.getTitre() + " (" +
                                    f.getGenre() + ", " + f.getDuree() + " min)"));
                            break;

                        case "2":
                            System.out.println("\n=== Séances disponibles ===");
                            seances.forEach(s -> {
                                int placesRestantes = s.getSalle().getCapacite() - s.getPlacesOccupees().size();
                                System.out.println(s.getId() + " : " + s.getFilm().getTitre() + " à " + s.getHeure() +
                                        " dans " + s.getSalle().getType() + " (Places restantes : " + placesRestantes + ")");
                            });
                            break;

                        case "3":
                            System.out.print("ID de la séance à réserver : ");
                            String seanceId = sc.nextLine();
                            Optional<Seance> seanceOpt = reservationService.getSeanceById(seanceId);
                            if (seanceOpt.isEmpty()) {
                                System.out.println("Séance introuvable !");
                                break;
                            }
                            Seance seance = seanceOpt.get();
                            int placesRestantes = seance.getSalle().getCapacite() - seance.getPlacesOccupees().size();
                            System.out.print("Nombre de places (max " + placesRestantes + ") : ");
                            try {
                                int nbTickets = Integer.parseInt(sc.nextLine());
                                Reservation r = reservationService.reserver(seance.getId(), userFinal, nbTickets);
                                System.out.println("Réservation réussie : " + r.getPlaces());
                            } catch (Exception ex) {
                                System.out.println("Erreur de réservation : " + ex.getMessage());
                            }
                            break;

                        case "4":
                            System.out.print("Entrez l'ID de la réservation à annuler : ");
                            String resId = sc.nextLine();
                            boolean annule = reservationService.annulerReservation(resId, userFinal);
                            System.out.println(annule ? "Réservation annulée !" : "Réservation introuvable ou non autorisée !");
                            break;

                        case "5":
                            System.out.println("\n=== Vos réservations ===");
                            reservationService.getAllReservations().stream()
                                    .filter(r -> r.getUser().getId().equals(userFinal.getId()))
                                    .forEach(System.out::println);
                            break;

                        case "6": // Modifier profil
                            LocalDate lastUpdate = userFinal.getLastProfileUpdate();
                            if (lastUpdate != null && ChronoUnit.DAYS.between(lastUpdate, LocalDate.now()) < 14) {
                                System.out.println("Vous pouvez modifier votre profil seulement toutes les 2 semaines.");
                                break;
                            }
                            System.out.print("Nouveau prénom (laisser vide pour garder actuel) : ");
                            String newPrenom = sc.nextLine();
                            System.out.print("Nouveau nom (laisser vide pour garder actuel) : ");
                            String newNom = sc.nextLine();
                            System.out.print("Nouvel email (laisser vide pour garder actuel) : ");
                            String newEmail = sc.nextLine();
                            System.out.print("Nouveau mot de passe (laisser vide pour garder actuel) : ");
                            String newPass = sc.nextLine();

                            if (!newPrenom.isBlank()) userFinal.setPrenom(newPrenom);
                            if (!newNom.isBlank()) userFinal.setNom(newNom);
                            if (!newEmail.isBlank()) userFinal.setEmail(newEmail);
                            if (!newPass.isBlank()) userFinal.setPasswordHash(newPass);

                            userFinal.setLastProfileUpdate(LocalDate.now());
                            utilisateurDAO.updateUtilisateur(userFinal);
                            System.out.println("Profil mis à jour !");
                            break;

                        case "7": // Supprimer compte
                            System.out.print("Confirmez votre email : ");
                            String emailConfirm = sc.nextLine();
                            System.out.print("Confirmez votre mot de passe : ");
                            String passConfirm = sc.nextLine();

                            if (emailConfirm.equals(userFinal.getEmail()) && passConfirm.equals(userFinal.getPasswordHash())) {
                                utilisateurDAO.deleteUtilisateur(userFinal.getId());
                                System.out.println("Compte supprimé !");
                                sessionOn = false;
                            } else {
                                System.out.println("Email ou mot de passe incorrect.");
                            }
                            break;

                        case "8":
                            sessionOn = false;
                            System.out.println("Déconnexion effectuée.");
                            break;

                        default:
                            System.out.println("Choix invalide !");
                    }
                }
            }

        } catch (SQLException | DataAccessException e) {
            System.out.println("Erreur critique : " + e.getMessage());
            e.printStackTrace();
        }

        sc.close();
        ioExecutor.shutdown();
    }
}
