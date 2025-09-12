package com.portailconge.portail_conge.repository;

import com.portailconge.portail_conge.model.Departement;
import com.portailconge.portail_conge.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    List<Utilisateur> findAllByRoleIgnoreCase(String role);

    @Query("SELECT u FROM Utilisateur u WHERE UPPER(u.role) = UPPER('RESPONSABLE')")
    List<Utilisateur> findAllResponsables();

    @Query("SELECT u FROM Utilisateur u WHERE UPPER(u.role) LIKE %:role%")
    List<Utilisateur> findAllResponsablesLike(@Param("role") String role);

    List<Utilisateur> findByDepartement(Departement departement);

}
