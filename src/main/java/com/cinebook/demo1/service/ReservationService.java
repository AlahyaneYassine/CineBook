package com.cinebook.demo1.service;

import com.cinebook.demo1.exception.ReservationException;
import com.cinebook.demo1.model.Reservation;
import com.cinebook.demo1.model.Seance;
import com.cinebook.demo1.model.Utilisateur;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class ReservationService {

    private final Map<String, Seance> seanceMap;
    private final List<Reservation> reservations = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, ReentrantLock> seanceLocks = new ConcurrentHashMap<>();

    public ReservationService(Collection<Seance> seances) {
        this.seanceMap = seances.stream()
                .collect(Collectors.toConcurrentMap(Seance::getId, s -> s));

        // Crée un lock pour chaque séance
        seanceLocks.putAll(
                seanceMap.keySet().stream()
                        .collect(Collectors.toMap(id -> id, id -> new ReentrantLock()))
        );
    }

    private ReentrantLock lockFor(String seanceId) {
        return seanceLocks.computeIfAbsent(seanceId, id -> new ReentrantLock());
    }

    // ================================
    //        RÉSERVER DES PLACES
    // ================================
    public Reservation reserver(String seanceId, Utilisateur user, int nbTickets) {

        Seance s = seanceMap.get(seanceId);
        if (s == null) {
            throw new ReservationException("Séance introuvable : " + seanceId);
        }

        if (nbTickets <= 0) {
            throw new ReservationException("Nombre de tickets invalide.");
        }

        ReentrantLock lock = lockFor(seanceId);
        lock.lock();

        try {
            int capacity = s.getSalle().getCapacite();
            Set<Integer> occupied = new HashSet<>(s.getPlacesOccupees());

            if (occupied.size() + nbTickets > capacity) {
                throw new ReservationException("Pas assez de places disponibles.");
            }

            // Attribution automatique des premières places disponibles
            List<Integer> assigned = new ArrayList<>();
            for (int seat = 1; seat <= capacity && assigned.size() < nbTickets; seat++) {
                if (!occupied.contains(seat)) assigned.add(seat);
            }

            if (assigned.size() != nbTickets) {
                throw new ReservationException("Erreur d’assignation automatique.");
            }

            // Mise à jour de la séance
            s.addPlaces(assigned);

            Reservation reservation = new Reservation(
                    UUID.randomUUID().toString(),
                    user,
                    s,
                    assigned,
                    LocalDateTime.now()
            );

            reservations.add(reservation);
            return reservation;

        } finally {
            lock.unlock();
        }
    }

    // ================================
    //       ANNULER UNE RÉSERVATION
    // ================================
    public boolean annulerReservation(String reservationId, Utilisateur user) {

        synchronized (reservations) {
            Optional<Reservation> opt = reservations.stream()
                    .filter(r ->
                            r.getId().equals(reservationId)
                                    && r.getUser().getId().equals(user.getId())
                    )
                    .findFirst();

            if (opt.isEmpty()) return false;

            Reservation r = opt.get();
            ReentrantLock lock = lockFor(r.getSeance().getId());
            lock.lock();

            try {
                r.getSeance().removePlaces(r.getPlaces());
                reservations.remove(r);
                return true;

            } finally {
                lock.unlock();
            }
        }
    }

    // ================================
    //     LISTE TOUTES RÉSERVATIONS
    // ================================
    public List<Reservation> getAllReservations() {
        synchronized (reservations) {
            return List.copyOf(reservations);
        }
    }

    // ================================
    //      RÉCUPÉRER SÉANCE PAR ID
    // ================================
    public Optional<Seance> getSeanceById(String id) {
        return Optional.ofNullable(seanceMap.get(id));
    }

    // ================================
    //       TAUX DE REMPLISSAGE
    // ================================
    public double tauxRemplissage(String seanceId) {
        Seance s = seanceMap.get(seanceId);
        if (s == null) return 0;

        return (s.getPlacesOccupees().size() * 100.0)
                / s.getSalle().getCapacite();
    }
}
