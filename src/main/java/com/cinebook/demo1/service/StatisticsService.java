package com.cinebook.demo1.service;

import com.cinebook.demo1.model.Film;
import com.cinebook.demo1.model.Reservation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Exemples d'utilisation de Java Streams pour les statistiques demandées.
 */
public class StatisticsService {

    private final Collection<Reservation> reservations;

    public StatisticsService(Collection<Reservation> reservations) {
        this.reservations = reservations;
    }

    // Film le plus réservé (par nombre de réservations)
    public Optional<Film> filmLePlusReserve() {
        return reservations.stream()
                .collect(Collectors.groupingBy(r -> r.getSeance().getFilm(), Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    // Nombre de tickets vendus par jour
    public Map<String, Long> ticketsParJour() {
        return reservations.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getDateReservation().toLocalDate().toString(),
                        Collectors.summingLong(r -> r.getPlaces().size())
                ));
    }

    // Top N films par tickets vendus
    public List<Map.Entry<Film, Long>> topFilmsParTickets(int topN) {
        Map<Film, Long> counts = reservations.stream()
                .flatMap(r -> r.getPlaces().stream().map(p -> r.getSeance().getFilm()))
                .collect(Collectors.groupingBy(f -> f, Collectors.counting()));

        return counts.entrySet().stream()
                .sorted(Map.Entry.<Film, Long>comparingByValue().reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }
}
