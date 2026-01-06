package com.cinebook.demo1.app;

import com.cinebook.demo1.dao.UtilisateurDAO;
import com.cinebook.demo1.exception.DataAccessException;
import com.cinebook.demo1.model.Utilisateur;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class AdminPanel {

    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/projet_java_db?useSSL=false&serverTimezone=UTC";
        String dbUser = "root";
        String dbPassword = "root";

        try (Connection conn = DriverManager.getConnection(url, dbUser, dbPassword);
             Scanner sc = new Scanner(System.in)) {

            UtilisateurDAO utilisateurDAO = new UtilisateurDAO(conn);
            Utilisateur adminUser = null;

            // ================== Connexion Admin ==================
            while (adminUser == null) {
                System.out.println("=== Connexion Admin ===");
                System.out.print("Username : ");
                String username = sc.nextLine();
                System.out.print("Mot de passe : ");
                String password = sc.nextLine();

                try {
                    Utilisateur u = utilisateurDAO.readUtilisateurByUsername(username);
                    if (u != null && u.getPasswordHash().equals(password) && "ADMIN".equals(u.getRole())) {
                        adminUser = u;
                        System.out.println("Connexion réussie ! Bienvenue, " + adminUser.getUsername());
                    } else {
                        System.out.println("Username, mot de passe incorrect ou rôle non admin.");
                    }
                } catch (DataAccessException e) {
                    System.out.println("Erreur lors de la connexion : " + e.getMessage());
                }
            }

            // ================== Menu Admin - Utilisateurs ==================
            boolean running = true;
            while (running) {
                System.out.println("\n=== Admin Panel - Gestion des Utilisateurs ===");
                System.out.println("1. CREATE Utilisateur");
                System.out.println("2. READ ALL Utilisateurs");
                System.out.println("3. READ Utilisateur par Username");
                System.out.println("4. UPDATE Utilisateur");
                System.out.println("5. DELETE Utilisateur par Username");
                System.out.println("6. QUIT");
                System.out.print("Choix : ");

                String choix = sc.nextLine();
                switch (choix) {

                    case "1": // CREATE
                        try {
                            System.out.print("Username : ");
                            String newUsername = sc.nextLine();

                            if (utilisateurDAO.readUtilisateurByUsername(newUsername) != null) {
                                System.out.println("❌ Username déjà utilisé !");
                                break;
                            }

                            System.out.print("Mot de passe : ");
                            String newPassword = sc.nextLine();

                            System.out.print("Rôle (ADMIN / CLIENT) : ");
                            String newRole = sc.nextLine().toUpperCase();

                            System.out.print("Nom : ");
                            String newNom = sc.nextLine();
                            System.out.print("Prénom : ");
                            String newPrenom = sc.nextLine();
                            System.out.print("Email : ");
                            String newEmail = sc.nextLine();

                            Utilisateur newUser = new Utilisateur(
                                    UUID.randomUUID().toString(),
                                    newUsername,
                                    newPassword,
                                    newRole,
                                    newNom,
                                    newPrenom,
                                    newEmail,
                                    LocalDate.now()
                            );

                            utilisateurDAO.createUtilisateur(newUser);
                            System.out.println("✅ Utilisateur créé : " + newUser);

                        } catch (DataAccessException e) {
                            System.out.println("Erreur création utilisateur : " + e.getMessage());
                        }
                        break;

                    case "2": // READ ALL
                        try {
                            List<Utilisateur> users = utilisateurDAO.readAllUtilisateurs();
                            System.out.println("\n📋 Liste des utilisateurs :");
                            if (users.isEmpty()) {
                                System.out.println("Aucun utilisateur trouvé.");
                            } else {
                                users.forEach(System.out::println);
                            }
                        } catch (DataAccessException e) {
                            System.out.println("Erreur lecture utilisateurs : " + e.getMessage());
                        }
                        break;

                    case "3": // READ BY USERNAME
                        try {
                            System.out.print("Username : ");
                            String unameRead = sc.nextLine();
                            Utilisateur u = utilisateurDAO.readUtilisateurByUsername(unameRead);
                            if (u == null) {
                                System.out.println("❌ Utilisateur introuvable");
                            } else {
                                System.out.println("✅ Utilisateur : " + u);
                            }
                        } catch (DataAccessException e) {
                            System.out.println("Erreur lecture utilisateur : " + e.getMessage());
                        }
                        break;

                    case "4": // UPDATE
                        try {
                            System.out.print("Username de l'utilisateur à modifier : ");
                            String unameUpdate = sc.nextLine();
                            Utilisateur exist = utilisateurDAO.readUtilisateurByUsername(unameUpdate);

                            if (exist == null) {
                                System.out.println("❌ Utilisateur introuvable");
                                break;
                            }

                            System.out.println("Username (immutable) : " + exist.getUsername());

                            System.out.print("Nouveau mot de passe (laisser vide pour garder) : ");
                            String updPass = sc.nextLine();
                            if (!updPass.isBlank()) exist.setPasswordHash(updPass);

                            System.out.print("Nouveau rôle (ADMIN / CLIENT, laisser vide pour garder) : ");
                            String updRole = sc.nextLine();
                            if (!updRole.isBlank()) exist.setRole(updRole.toUpperCase());

                            System.out.print("Nouveau nom : ");
                            String updNom = sc.nextLine();
                            if (!updNom.isBlank()) exist.setNom(updNom);

                            System.out.print("Nouveau prénom : ");
                            String updPrenom = sc.nextLine();
                            if (!updPrenom.isBlank()) exist.setPrenom(updPrenom);

                            System.out.print("Nouvel email : ");
                            String updEmail = sc.nextLine();
                            if (!updEmail.isBlank()) exist.setEmail(updEmail);

                            exist.setLastProfileUpdate(LocalDate.now());
                            utilisateurDAO.updateUtilisateur(exist);

                            System.out.println("✏️ Utilisateur mis à jour : " + exist);

                        } catch (DataAccessException e) {
                            System.out.println("Erreur mise à jour utilisateur : " + e.getMessage());
                        }
                        break;

                    case "5": // DELETE BY USERNAME
                        try {
                            System.out.print("Username de l'utilisateur à supprimer : ");
                            String unameDel = sc.nextLine();
                            Utilisateur userDel = utilisateurDAO.readUtilisateurByUsername(unameDel);
                            if (userDel != null) {
                                utilisateurDAO.deleteUtilisateur(userDel.getId());
                                System.out.println("🗑️ Utilisateur supprimé !");
                            } else {
                                System.out.println("❌ Utilisateur introuvable !");
                            }
                        } catch (DataAccessException e) {
                            System.out.println("Erreur suppression utilisateur : " + e.getMessage());
                        }
                        break;

                    case "6":
                        running = false;
                        System.out.println("👋 Déconnexion admin");
                        break;

                    default:
                        System.out.println("Option invalide !");
                }
            }

        } catch (SQLException e) {
            System.out.println("Erreur connexion DB : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
