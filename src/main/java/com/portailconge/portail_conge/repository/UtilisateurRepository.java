package com.portailconge.portail_conge.repository;

import com.portailconge.portail_conge.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {

    // Trouver par email
    Optional<Utilisateur> findByEmail(String email);

    // Trouver par matricule (ou username)
    Utilisateur findByMatricule(String matricule);

    // Trouver par rôle (ex: DIRECTEUR)
    Utilisateur findByRole(String role);

    // Compter le nombre d'utilisateurs par rôle (ex: PERSONNEL)
    int countByRole(String role);

    // Optionnel : récupérer tous les utilisateurs d'un rôle
    List<Utilisateur> findAllByRole(String role);
}
