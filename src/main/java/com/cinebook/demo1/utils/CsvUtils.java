package com.cinebook.demo1.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public final class CsvUtils {

    private CsvUtils() {}

    public static Stream<String> lines(Path path) {
        try {
            return Files.lines(path);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lecture CSV " + path, e);
        }
    }

    // helper to split CSV line with ; allowing trimming
    public static String[] split(String line) {
        return Stream.of(line.split(";"))
                .map(String::trim)
                .toArray(String[]::new);
    }
}
