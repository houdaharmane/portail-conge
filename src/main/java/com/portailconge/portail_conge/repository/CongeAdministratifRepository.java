package com.portailconge.portail_conge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.portailconge.portail_conge.model.CongeAdministratif;
import com.portailconge.portail_conge.model.StatutDemande;
import com.portailconge.portail_conge.model.Utilisateur;

public interface CongeAdministratifRepository extends JpaRepository<CongeAdministratif, Long> {

    List<CongeAdministratif> findByUtilisateurAndAnnee(Utilisateur utilisateur, int annee);

    List<CongeAdministratif> findByUtilisateurAndAnneeAndStatut(Utilisateur utilisateur, int annee,
            StatutDemande statut);

    List<CongeAdministratif> findByUtilisateurAndStatut(Utilisateur utilisateur, StatutDemande statut);

}
