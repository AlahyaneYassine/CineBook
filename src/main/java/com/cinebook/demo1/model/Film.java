package com.cinebook.demo1.model;


import java.util.Objects;

public class Film {

    private final String id;
    private String titre;
    private String genre;
    private int duree; // en minutes
    private int ageRestriction;

    public Film(String id, String titre, String genre, int duree, int ageRestriction) {
        this.id = Objects.requireNonNull(id, "ID film ne peut pas être null");
        this.titre = titre;
        this.genre = genre;
        this.duree = duree;
        this.ageRestriction = ageRestriction;
    }

    // ================== GETTERS / SETTERS ==================

    public String getId() { return id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public int getDuree() { return duree; }
    public void setDuree(int duree) {
        if (duree > 0) this.duree = duree;
    }

    public int getAgeRestriction() { return ageRestriction; }
    public void setAgeRestriction(int ageRestriction) {
        if (ageRestriction >= 0) this.ageRestriction = ageRestriction;
    }

    // ================== EQUALS & HASHCODE ==================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Film)) return false;
        Film film = (Film) o;
        return id.equals(film.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // ================== TO STRING ==================

    @Override
    public String toString() {
        return "Film{" +
                "id='" + id + '\'' +
                ", titre='" + titre + '\'' +
                ", genre='" + genre + '\'' +
                ", durée=" + duree + " min" +
                ", ageMin=" + ageRestriction +
                '}';
    }
}
