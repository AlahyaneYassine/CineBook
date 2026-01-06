package com.cinebook.demo1.app;

import com.cinebook.demo1.dao.SalleDAO;
import com.cinebook.demo1.dao.UtilisateurDAO;
import com.cinebook.demo1.exception.DataAccessException;
import com.cinebook.demo1.model.Salle;
import com.cinebook.demo1.model.Utilisateur;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class TestSalleDAO {

    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/projet_java_db?useSSL=false&serverTimezone=UTC";
        String dbUser = "root";
        String dbPassword = "root"; // change si besoin

        try (Connection conn = DriverManager.getConnection(url, dbUser, dbPassword);
             Scanner sc = new Scanner(System.in)) {

            UtilisateurDAO utilisateurDAO = new UtilisateurDAO(conn);
            SalleDAO salleDAO = new SalleDAO(conn);
            Utilisateur admin = null;

            // ================== CONNEXION ADMIN ==================
            while (admin == null) {
                System.out.println("=== Connexion Admin ===");
                System.out.print("Username : ");
                String username = sc.nextLine();
                System.out.print("Mot de passe : ");
                String password = sc.nextLine();

                try {
                    Utilisateur u = utilisateurDAO.readUtilisateurByUsername(username);
                    if (u != null && u.getPasswordHash().equals(password) && "ADMIN".equals(u.getRole())) {
                        admin = u;
                        System.out.println("Connexion réussie ! Bienvenue, " + admin.getUsername());
                    } else {
                        System.out.println("❌ Username ou mot de passe incorrect, ou rôle non ADMIN.");
                    }
                } catch (DataAccessException e) {
                    System.out.println("Erreur lors de la connexion : " + e.getMessage());
                }
            }

            // ================== MENU CRUD SALLES ==================
            boolean running = true;
            while (running) {

                System.out.println("\n=== ADMIN PANEL - SALLES ===");
                System.out.println("1. CREATE Salle");
                System.out.println("2. READ ALL Salles");
                System.out.println("3. READ Salle par ID");
                System.out.println("4. UPDATE Salle");
                System.out.println("5. DELETE Salle");
                System.out.println("6. QUIT");
                System.out.print("Choix : ");

                String choix = sc.nextLine();

                try {
                    switch (choix) {

                        case "1": // CREATE
                            System.out.print("Capacité : ");
                            int capacite = Integer.parseInt(sc.nextLine());
                            System.out.print("Type (2D / 3D / IMAX) : ");
                            String type = sc.nextLine();

                            Salle salle = new Salle(
                                    "S-" + UUID.randomUUID().toString().substring(0, 6),
                                    capacite,
                                    type
                            );

                            salleDAO.createSalle(salle);
                            System.out.println("✅ Salle créée : " + salle);
                            break;

                        case "2": // READ ALL
                            List<Salle> salles = salleDAO.readAllSalles();
                            System.out.println("\n=== LISTE DES SALLES ===");
                            if (salles.isEmpty()) {
                                System.out.println("Aucune salle trouvée.");
                            } else {
                                salles.forEach(System.out::println);
                            }
                            break;

                        case "3": // READ BY ID
                            System.out.print("ID de la salle : ");
                            String idRead = sc.nextLine();
                            Salle s = salleDAO.readSalleById(idRead);
                            if (s == null) {
                                System.out.println("❌ Salle introuvable");
                            } else {
                                System.out.println("✅ Salle trouvée : " + s);
                            }
                            break;

                        case "4": // UPDATE
                            System.out.print("ID de la salle à modifier : ");
                            String idUpdate = sc.nextLine();
                            Salle exist = salleDAO.readSalleById(idUpdate);
                            if (exist == null) {
                                System.out.println("❌ Salle introuvable");
                                break;
                            }

                            System.out.print("Nouvelle capacité : ");
                            int newCap = Integer.parseInt(sc.nextLine());
                            System.out.print("Nouveau type : ");
                            String newType = sc.nextLine();

                            Salle updated = new Salle(idUpdate, newCap, newType);
                            salleDAO.updateSalle(updated);
                            System.out.println("✏️ Salle mise à jour");
                            break;

                        case "5": // DELETE
                            System.out.print("ID de la salle à supprimer : ");
                            String idDelete = sc.nextLine();
                            salleDAO.deleteSalle(idDelete);
                            System.out.println("🗑️ Salle supprimée");
                            break;

                        case "6":
                            running = false;
                            System.out.println("👋 Fermeture du panel ADMIN");
                            break;

                        default:
                            System.out.println("Choix invalide");
                    }
                } catch (DataAccessException e) {
                    System.out.println("❌ Erreur DAO : " + e.getMessage());
                }
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur connexion DB : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
