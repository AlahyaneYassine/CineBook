-- Schéma de la base de données CineBook
-- Reconstruit à partir des requêtes SQL présentes dans les classes DAO
-- (com.cinebook.demo1.dao.*), à défaut d'un script fourni dans le repo original.

CREATE DATABASE IF NOT EXISTS projet_java_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE projet_java_db;

-- ==================== FILM ====================
CREATE TABLE IF NOT EXISTS film (
    id             VARCHAR(64) PRIMARY KEY,
    titre          VARCHAR(255) NOT NULL,
    genre          VARCHAR(100),
    duree          INT NOT NULL,
    ageRestriction INT NOT NULL DEFAULT 0
);

-- ==================== SALLE ====================
CREATE TABLE IF NOT EXISTS salle (
    id       VARCHAR(64) PRIMARY KEY,
    capacite INT NOT NULL,
    type     VARCHAR(100)
);

-- ==================== SEANCE ====================
CREATE TABLE IF NOT EXISTS seance (
    id       VARCHAR(64) PRIMARY KEY,
    film_id  VARCHAR(64) NOT NULL,
    salle_id VARCHAR(64) NOT NULL,
    date     DATE NOT NULL,
    heure    TIME NOT NULL,
    tarif    DOUBLE NOT NULL,
    FOREIGN KEY (film_id)  REFERENCES film(id)  ON DELETE CASCADE,
    FOREIGN KEY (salle_id) REFERENCES salle(id) ON DELETE CASCADE
);

-- ==================== UTILISATEUR ====================
CREATE TABLE IF NOT EXISTS utilisateur (
    id                 VARCHAR(64) PRIMARY KEY,
    username           VARCHAR(100) NOT NULL UNIQUE,
    passwordHash       VARCHAR(255) NOT NULL,
    role               VARCHAR(20)  NOT NULL, -- ex: 'CLIENT' / 'ADMIN'
    nom                VARCHAR(100),
    prenom             VARCHAR(100),
    email              VARCHAR(255) UNIQUE,
    lastProfileUpdate  DATE
);

-- ==================== RESERVATION ====================
CREATE TABLE IF NOT EXISTS reservation (
    id               VARCHAR(64) PRIMARY KEY,
    user_username    VARCHAR(100) NOT NULL,
    seance_id        VARCHAR(64) NOT NULL,
    date_reservation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_username) REFERENCES utilisateur(username) ON DELETE CASCADE,
    FOREIGN KEY (seance_id)     REFERENCES seance(id)             ON DELETE CASCADE
);

-- ==================== RESERVATION_PLACE ====================
-- Une ligne par siège réservé dans une réservation (réservation multi-places)
CREATE TABLE IF NOT EXISTS reservation_place (
    reservation_id VARCHAR(64) NOT NULL,
    seance_id      VARCHAR(64) NOT NULL,
    place_num      INT NOT NULL,
    PRIMARY KEY (reservation_id, place_num),
    FOREIGN KEY (reservation_id) REFERENCES reservation(id) ON DELETE CASCADE,
    FOREIGN KEY (seance_id)      REFERENCES seance(id)      ON DELETE CASCADE
);
