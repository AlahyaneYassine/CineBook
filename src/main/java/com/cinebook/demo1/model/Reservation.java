package com.cinebook.demo1.model;

import java.time.LocalDateTime;
import java.util.List;

public class Reservation {

    private final String id;
    private final Utilisateur user;
    private final Seance seance;
    private final List<Integer> places;
    private final LocalDateTime dateReservation;

    public Reservation(String id, Utilisateur user, Seance seance,
                       List<Integer> places, LocalDateTime dateReservation) {
        this.id = id;
        this.user = user;
        this.seance = seance;
        this.places = List.copyOf(places); // ✅ protège l’état
        this.dateReservation = dateReservation;
    }

    public String getId() { return id; }
    public Utilisateur getUser() { return user; }
    public Seance getSeance() { return seance; }
    public List<Integer> getPlaces() { return places; }
    public LocalDateTime getDateReservation() { return dateReservation; }

    @Override
    public String toString() {
        return "Reservation{" +
                "id='" + id + '\'' +
                ", user=" + user.getUsername() +
                ", seance=" + seance.getId() +
                ", places=" + places +
                ", dateReservation=" + dateReservation +
                '}';
    }
}
