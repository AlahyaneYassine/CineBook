package com.cinebook.demo1.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Seance {

    private final String id;
    private final Film film;
    private final Salle salle;
    private final LocalDate date;
    private final LocalTime heure;
    private final double tarif;

    // Liste thread-safe
    private final List<Integer> placesOccupees = new CopyOnWriteArrayList<>();

    public Seance(String id, Film film, Salle salle, LocalDate date, LocalTime heure, double tarif) {
        this.id = id;
        this.film = film;
        this.salle = salle;
        this.date = date;
        this.heure = heure;
        this.tarif = tarif;
    }

    // ==== GETTERS =====================================================

    public String getId() { return id; }
    public Film getFilm() { return film; }
    public Salle getSalle() { return salle; }
    public LocalDate getDate() { return date; }
    public LocalTime getHeure() { return heure; }
    public double getTarif() { return tarif; }

    // Retourne une liste immuable
    public List<Integer> getPlacesOccupees() {
        return Collections.unmodifiableList(placesOccupees);
    }

    // ==== GESTION DES PLACES (Thread-Safe) =============================

    public void addPlaces(Collection<Integer> places) {
        for (Integer p : places) {
            if (!placesOccupees.contains(p)) {
                placesOccupees.add(p);
            }
        }
    }

    public void removePlaces(Collection<Integer> places) {
        placesOccupees.removeAll(places);
    }

    // ==== INFOS PRATIQUES ==============================================

    public boolean isFull() {
        return placesOccupees.size() >= salle.getCapacite();
    }

    public int getPlacesDisponibles() {
        return salle.getCapacite() - placesOccupees.size();
    }

    @Override
    public String toString() {
        return "Seance{" +
                "id='" + id + '\'' +
                ", film=" + film.getTitre() +
                ", salle=" + salle.getId() +
                ", date=" + date +
                ", heure=" + heure +
                ", tarif=" + tarif +
                ", placesOccupees=" + placesOccupees.size() +
                "/" + salle.getCapacite() +
                '}';
    }
}
