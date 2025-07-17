package com.portailconge.portail_conge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.portailconge.portail_conge.model.Utilisateur;

import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {
    Optional<Utilisateur> findByEmail(String email);
}
