package com.portailconge.portail_conge.repository;

import com.portailconge.portail_conge.model.DemandeConge;
import com.portailconge.portail_conge.model.Departement;
import com.portailconge.portail_conge.model.StatutDemande;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandeCongeRepository extends JpaRepository<DemandeConge, Long> {

    @Query("SELECT d FROM DemandeConge d WHERE d.demandeur.departement = :departement AND d.statut = :statut")
    List<DemandeConge> findDemandesEnAttenteByDepartement(@Param("departement") Departement departement,
            @Param("statut") StatutDemande statut);

}
