package com.cinebook.demo1.model;


import java.util.Objects;

public class Salle {

    private final String id;       // identifiant unique de la salle
    private final int capacite;    // nombre maximal de places
    private final String type;     // type de salle : 2D, 3D, IMAX

    public Salle(String id, int capacite, String type) {
        this.id = Objects.requireNonNull(id, "ID de la salle ne peut pas être null");
        this.capacite = capacite;
        this.type = Objects.requireNonNull(type, "Type de salle ne peut pas être null");
    }

    // ================== GETTERS ==================
    public String getId() { return id; }
    public int getCapacite() { return capacite; }
    public String getType() { return type; }

    // ================== EQUALS & HASHCODE ==================
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Salle)) return false;
        Salle s = (Salle) o;
        return id.equals(s.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // ================== TO STRING ==================
    @Override
    public String toString() {
        return "Salle{" + id + ", capacite=" + capacite + ", type=" + type + "}";
    }
}
