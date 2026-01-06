package com.cinebook.demo1.model;

import java.time.LocalDate;
import java.util.Objects;

public class Utilisateur {

    private final String id;
    private final String username; // immuable
    private String passwordHash;   // mot de passe modifiable
    private String role;           // rôle modifiable (ADMIN / CLIENT)
    private LocalDate lastProfileUpdate;
    private String nom;
    private String prenom;
    private String email;

    public Utilisateur(String id, String username, String passwordHash, String role,
                       String nom, String prenom, String email, LocalDate lastProfileUpdate) {
        this.id = Objects.requireNonNull(id, "ID ne peut pas être null");
        this.username = Objects.requireNonNull(username, "Username ne peut pas être null");
        this.passwordHash = passwordHash;
        this.role = role;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.lastProfileUpdate = lastProfileUpdate;
    }

    // ================== GETTERS ==================
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
    public LocalDate getLastProfileUpdate() { return lastProfileUpdate; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }

    // ================== SETTERS ==================
    public void setPasswordHash(String passwordHash) {
        if (passwordHash != null) this.passwordHash = passwordHash;
    }

    public void setRole(String role) {
        if (role != null) this.role = role;
    }

    public void setNom(String nom) {
        if (nom != null) this.nom = nom;
    }

    public void setPrenom(String prenom) {
        if (prenom != null) this.prenom = prenom;
    }

    public void setEmail(String email) {
        if (email != null) this.email = email;
    }

    public void setLastProfileUpdate(LocalDate date) {
        this.lastProfileUpdate = date;
    }

    // ================== TO STRING ==================
    @Override
    public String toString() {
        return "Utilisateur{" +
                "username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
