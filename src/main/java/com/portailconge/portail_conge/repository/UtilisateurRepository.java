package com.portailconge.portail_conge.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.portailconge.portail_conge.model.Utilisateur;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {
    Optional<Utilisateur> findByEmail(String email);
}
