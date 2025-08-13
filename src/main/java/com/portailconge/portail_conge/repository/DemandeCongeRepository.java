package com.portailconge.portail_conge.repository;

import com.portailconge.portail_conge.model.DemandeConge;
import com.portailconge.portail_conge.model.Departement;
import com.portailconge.portail_conge.model.StatutDemande;
import com.portailconge.portail_conge.model.Utilisateur;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface DemandeCongeRepository extends JpaRepository<DemandeConge, Long> {

        @Query("SELECT d FROM DemandeConge d WHERE d.demandeur.departement = :departement AND d.statut = :statut")
        List<DemandeConge> findDemandesEnAttenteByDepartement(@Param("departement") Departement departement,
                        @Param("statut") StatutDemande statut);

        // Pour afficher les demandes faites par un responsable en attente de validation
        // RH
        @Query("SELECT d FROM DemandeConge d WHERE d.demandeur.role = 'RESPONSABLE' AND d.statut = 'EN_ATTENTE_RH'")
        List<DemandeConge> findDemandesResponsablePourRH();

        // Pour afficher les demandes approuvées par le responsable (statut APPROUVEE)
        @Query("SELECT d FROM DemandeConge d WHERE d.statut = 'APPROUVEE'")
        List<DemandeConge> findDemandesApprouvees();

        List<DemandeConge> findByDemandeur(Utilisateur demandeur);

        List<DemandeConge> findByStatut(StatutDemande statut);

        List<DemandeConge> findDemandesByDepartementAndStatut(Departement departement, StatutDemande statut);

        int countByStatut(StatutDemande statut);

        int countByStatutIn(List<StatutDemande> statuts);

        List<DemandeConge> findByStatutIn(List<StatutDemande> statuts);

        Page<DemandeConge> findByStatutIn(List<StatutDemande> statuts, Pageable pageable);

        @Query("SELECT d FROM DemandeConge d WHERE d.demandeur.role = :role")
        List<DemandeConge> findByDemandeurRole(@Param("role") String role);

        @Query("SELECT d FROM DemandeConge d WHERE d.demandeur.role = 'RESPONSABLE' AND d.statut NOT IN ('EN_ATTENTE', 'EN_ATTENTE_RH')")
        List<DemandeConge> findDemandesTraiteesFaitesParResponsable();

        @Query("SELECT d FROM DemandeConge d WHERE d.demandeur.role = 'RESPONSABLE' AND d.statut = :statut")
        List<DemandeConge> findDemandesResponsablesApprouvees(@Param("statut") StatutDemande statut);

        Page<DemandeConge> findByStatut(StatutDemande statut, Pageable pageable);

        List<DemandeConge> findByStatutAndDemandeurRole(StatutDemande statut, String role);

        List<DemandeConge> findByDemandeur_Role(String role);

        @Query("SELECT d FROM DemandeConge d WHERE d.demandeur.role = :role")
        Page<DemandeConge> findByDemandeurRole(@Param("role") String role, Pageable pageable);

        Page<DemandeConge> findByDemandeurRoleAndStatutIn(
                        String role,
                        List<StatutDemande> statuts,
                        Pageable pageable);

        Page<DemandeConge> findByDemandeurAndStatutIn(Utilisateur demandeur, List<StatutDemande> statuts,
                        Pageable pageable);

        @Query("SELECT d FROM DemandeConge d WHERE d.demandeur.departement = :departement AND d.statut = :statut AND d.demandeur.role = 'PERSONNEL'")
        List<DemandeConge> findDemandesEnAttentePersonnelByDepartement(@Param("departement") Departement departement,
                        @Param("statut") StatutDemande statut);

        @Query("SELECT d FROM DemandeConge d WHERE d.demandeur.departement = :departement AND d.demandeur.role = 'RESPONSABLE' AND d.statut = :statut")
        List<DemandeConge> findDemandesEnAttenteResponsableByDepartement(@Param("departement") Departement departement,
                        @Param("statut") StatutDemande statut);

        List<DemandeConge> findByDemandeurRoleAndStatut(String role, StatutDemande statut);

        List<DemandeConge> findByDemandeurRoleAndStatutIn(String role, List<StatutDemande> statuts);

}
