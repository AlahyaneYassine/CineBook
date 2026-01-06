package com.cinebook.demo1.dao;

import com.cinebook.demo1.exception.DataAccessException;
import com.cinebook.demo1.model.Film;
import com.cinebook.demo1.model.Reservation;
import com.cinebook.demo1.model.Salle;
import com.cinebook.demo1.model.Seance;
import com.cinebook.demo1.utils.CsvUtils;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class CsvDataManager {

    private final Path filmsPath;
    private final Path sallesPath;
    private final Path seancesPath;
    private final Path reservationsPath;
    private final ExecutorService ioExecutor;

    public CsvDataManager(Path baseDir, ExecutorService ioExecutor) {
        this.filmsPath = baseDir.resolve("films.csv");
        this.sallesPath = baseDir.resolve("salles.csv");
        this.seancesPath = baseDir.resolve("seances.csv");
        this.reservationsPath = baseDir.resolve("reservations.csv");
        this.ioExecutor = ioExecutor;
    }

    // ================================
    //       LOAD ASYNC METHODS
    // ================================
    public CompletableFuture<List<Film>> loadFilmsAsync() {
        return CompletableFuture.supplyAsync(this::loadFilms, ioExecutor);
    }

    public CompletableFuture<List<Salle>> loadSallesAsync() {
        return CompletableFuture.supplyAsync(this::loadSalles, ioExecutor);
    }

    public CompletableFuture<List<Seance>> loadSeancesAsync(
            Map<String, Film> filmMap,
            Map<String, Salle> salleMap) {

        return CompletableFuture.supplyAsync(
                () -> loadSeances(filmMap, salleMap),
                ioExecutor
        );
    }

    // ================================
    //       SYNCHRONOUS HELPERS
    // ================================
    private List<Film> loadFilms() {
        try {
            if (!Files.exists(filmsPath))
                return Collections.emptyList();

            return CsvUtils.lines(filmsPath)
                    .filter(l -> !l.isBlank())
                    .map(line -> {
                        String[] parts = CsvUtils.split(line);
                        // id;titre;genre;duree;ageRestriction
                        return new Film(
                                parts[0],
                                parts[1],
                                parts[2],
                                Integer.parseInt(parts[3]),
                                Integer.parseInt(parts[4])
                        );
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new DataAccessException("Impossible de charger films", e);
        }
    }

    private List<Salle> loadSalles() {
        try {
            if (!Files.exists(sallesPath))
                return Collections.emptyList();

            return CsvUtils.lines(sallesPath)
                    .filter(l -> !l.isBlank())
                    .map(line -> {
                        // id;capacite;type
                        String[] parts = CsvUtils.split(line);
                        return new Salle(
                                parts[0],
                                Integer.parseInt(parts[1]),
                                parts[2]
                        );
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new DataAccessException("Impossible de charger salles", e);
        }
    }

    private List<Seance> loadSeances(
            Map<String, Film> filmMap,
            Map<String, Salle> salleMap) {

        try {
            if (!Files.exists(seancesPath))
                return Collections.emptyList();

            return CsvUtils.lines(seancesPath)
                    .filter(l -> !l.isBlank())
                    .map(line -> {

                        // id;filmId;salleId;date;heure;tarif;occupiedSeats
                        String[] p = CsvUtils.split(line);

                        Film film = filmMap.get(p[1]);
                        Salle salle = salleMap.get(p[2]);

                        Seance seance = new Seance(
                                p[0],
                                film,
                                salle,
                                LocalDate.parse(p[3]),
                                LocalTime.parse(p[4]),
                                Double.parseDouble(p[5])
                        );

                        if (p.length > 6 && !p[6].isBlank()) {
                            String[] seats = p[6].split(",");
                            for (String seat : seats) {
                                try {
                                    seance.addPlaces(List.of(Integer.parseInt(seat.trim())));
                                } catch (Exception ignored) {
                                }
                            }
                        }
                        return seance;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new DataAccessException("Impossible de charger seances", e);
        }
    }

    // ================================
    //       WRITE RESERVATIONS
    // ================================
    public CompletableFuture<Void> writeReservationsAsync(
            Collection<Reservation> reservations) {

        return CompletableFuture.runAsync(
                () -> writeReservations(reservations),
                ioExecutor
        );
    }

    private void writeReservations(
            Collection<Reservation> reservations) {

        try {
            Files.createDirectories(reservationsPath.getParent());

            try (BufferedWriter writer = Files.newBufferedWriter(
                    reservationsPath,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {

                for (Reservation r : reservations) {

                    String places = r.getPlaces().stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(","));

                    writer.write(String.join(";",
                            r.getId(),
                            r.getUser().getId(),
                            r.getSeance().getId(),
                            places,
                            r.getDateReservation().toString()
                    ));
                    writer.newLine();
                }
            }

        } catch (Exception e) {
            throw new DataAccessException("Impossible d'écrire reservations", e);
        }
    }
}
