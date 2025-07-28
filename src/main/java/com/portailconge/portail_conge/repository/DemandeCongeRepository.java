package com.portailconge.portail_conge.repository;

import com.portailconge.portail_conge.model.DemandeConge;
import com.portailconge.portail_conge.model.StatutDemande;
import com.portailconge.portail_conge.model.Departement;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DemandeCongeRepository extends JpaRepository<DemandeConge, Long> {

    List<DemandeConge> findByStatutAndDemandeur_Departement(StatutDemande statut, Departement departement);

}
