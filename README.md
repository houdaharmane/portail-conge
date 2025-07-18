# Portail Conge

## Présentation

Portail Conge est une application web de gestion des congés pour les entreprises, développée avec Java (Spring Boot) et HTML. Elle permet aux utilisateurs de soumettre des demandes de congé, de gérer leur validation selon le rôle (Personnel, RH, Responsable, Directeur, Admission), et de consulter l’historique des validations.

## Fonctionnalités principales

- Authentification avec gestion de rôles (Personnel, RH, Responsable, Directeur, Admission)
- Soumission et validation des demandes de congé
- Tableau de bord pour chaque rôle (exemple : dashboard RH)
- Gestion des utilisateurs et départements
- Interface utilisateur personnalisée avec des templates HTML (ex : login, dashboard)

## Structure du projet

- **src/main/java/com/portailconge/portail_conge/**  
  - `controller` : Contrôleurs pour les différentes interfaces et rôles (LoginController, RhController, DirecteurController, etc.)
  - `model` : Entités principales (Utilisateur, DemandeConge, Departement, HistoriqueValidation)
  - `repository` : Accès aux données (UtilisateurRepository, DemandeCongeRepository, etc.)
  - `service` : Logique métier (DemandeCongeService, etc.)
  - `PortailCongeApplication.java` : Point d’entrée de l’application Spring Boot

- **src/main/resources/templates/**  
  - `login.html` : Page de connexion
  - `dashboard-rh.html` : Tableau de bord RH
  - Autres templates pour la navigation et la gestion des demandes

## Installation

1. **Cloner le dépôt :**
   ```bash
   git clone https://github.com/houdaharmane/portail-conge.git
   ```
2. **Installer les dépendances et compiler :**
   ```bash
   cd portail-conge
   ./mvnw install
   ```
3. **Configurer la base de données** (variables à définir dans `application.properties` pour la connexion et l’initialisation).

4. **Lancer l’application :**
   ```bash
   ./mvnw spring-boot:run
   ```
   Accédez à l’application sur [http://localhost:8080](http://localhost:8080).

## Technologies utilisées

- Java 17
- Spring Boot
- HTML / CSS (Thymeleaf)
- (Prévoir une base de données relationnelle, ex : MySQL ou H2)

## Contribution

Les contributions sont les bienvenues (Pull Request, Issue).  
Contactez le propriétaire du dépôt pour toute demande ou suggestion.

## Licence

Ce projet n’a pas encore de licence définie.

---
Pour plus d’informations, consultez le code source ou contactez [houdaharmane](https://github.com/houdaharmane).
