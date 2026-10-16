# CineBook

Application desktop de réservation de cinéma, développée en **Java/JavaFX** avec une base de données **MySQL**. Deux espaces distincts : un espace **client** (parcourir les films et séances, réserver des places, consulter ses réservations) et un espace **admin** (gestion des films, salles, séances, réservations et utilisateurs, statistiques).

## Fonctionnalités

**Côté client**
- Inscription / connexion
- Parcourir les films et les séances disponibles
- Réserver une ou plusieurs places pour une séance
- Consulter et annuler ses propres réservations
- Modifier son profil

**Côté admin**
- Gestion CRUD des films, salles et séances
- Gestion des utilisateurs (rôles `ADMIN` / `CLIENT`)
- Vue d'ensemble des réservations
- Statistiques (via `StatisticsService`)

## Stack technique

- **Java 24**, **JavaFX 17** (interface graphique, FXML)
- **Maven** (build, `javafx-maven-plugin`)
- **MySQL** (via `mysql-connector-j`) — accès en JDBC brut avec `PreparedStatement`, pattern **DAO**
- **ControlsFX** / **BootstrapFX** — composants UI additionnels
- **JUnit 5** — tests (scope test, pas encore de tests écrits)

## Architecture du code

```
src/main/java/com/cinebook/demo1/
├── model/        # Film, Salle, Seance, Utilisateur, Reservation
├── dao/          # Accès base de données (DAO), un par entité
├── service/      # Logique métier (FilmService, ReservationService, StatisticsService)
├── exception/    # Exceptions métier (DataAccessException, ReservationException)
├── ui/
│   ├── controller/   # Contrôleurs JavaFX (un par écran)
│   └── navigation/   # Navigator / SceneManager / ShellRouter
└── app/          # Prototype CLI antérieur (AppMain, AdminPanel...) basé sur des fichiers CSV,
                  # non utilisé par l'application finale — conservé à titre d'historique
```

L'application finale se lance via **`ui.MainApp`** (interface JavaFX + MySQL). Le package `app/` contient un prototype plus ancien, basé sur des fichiers CSV (`CsvDataManager`), qui n'est plus le point d'entrée du projet.

## Prérequis

- JDK 24
- Maven (ou le wrapper `./mvnw` fourni)
- MySQL Server (local, port 3306)

## Installation

1. **Créer la base de données** — exécuter `schema.sql` (fourni dans ce repo) dans MySQL :
   ```bash
   mysql -u root -p < schema.sql
   ```
   Cela crée la base `projet_java_db` et toutes les tables nécessaires.

2. **Configurer la connexion** — par défaut, la connexion pointe vers `localhost:3306/projet_java_db` avec l'utilisateur `root` (voir `DB.java`). Adapte ces valeurs à ton environnement si besoin.

3. **Créer un compte admin** (l'inscription via l'app crée uniquement des comptes `CLIENT`) :
   ```sql
   INSERT INTO utilisateur (id, username, passwordHash, role, nom, prenom, email)
   VALUES (UUID(), 'admin', 'motdepasse', 'ADMIN', 'Admin', 'Admin', 'admin@cinebook.local');
   ```

4. **Lancer l'application** :
   ```bash
   ./mvnw clean javafx:run
   ```
   
## Auteur

**Yassine Alahyane**
