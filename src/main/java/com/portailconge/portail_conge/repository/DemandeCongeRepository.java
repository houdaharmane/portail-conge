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

        @Query("SELECT d FROM DemandeConge d " +
                        "WHERE d.demandeur.departement = :departement " +
                        "AND d.statut = :statut " +
                        "AND d.demandeur.role = 'PERSONNEL'")
        List<DemandeConge> findDemandesEnAttentePersonnelByDepartement(@Param("departement") Departement departement,
                        @Param("statut") StatutDemande statut);

        @Query("SELECT d FROM DemandeConge d WHERE d.demandeur.departement = :departement AND d.demandeur.role = 'RESPONSABLE' AND d.statut = :statut")
        List<DemandeConge> findDemandesEnAttenteResponsableByDepartement(@Param("departement") Departement departement,
                        @Param("statut") StatutDemande statut);

        List<DemandeConge> findByDemandeurRoleAndStatut(String role, StatutDemande statut);

        List<DemandeConge> findByDemandeurRoleAndStatutIn(String role, List<StatutDemande> statuts);

        List<DemandeConge> findByStatutOrDemandeurRole(StatutDemande statut, String role);

        List<DemandeConge> findByDemandeurIdAndStatutIn(Long demandeurId, List<StatutDemande> statuts);

        List<DemandeConge> findByDemandeur_IdAndStatutIn(Long demandeurId, List<StatutDemande> statuts);

        // Toutes les demandes d’un utilisateur avec certains statuts
        List<DemandeConge> findByDemandeurAndStatutIn(Utilisateur demandeur, List<StatutDemande> statuts);

        @Query("SELECT d FROM DemandeConge d " +
                        "WHERE d.demandeur = :directeur " +
                        "   OR d.statut IN ('APPROUVEE_DIRECTEUR','REFUSEE_DIRECTEUR')")
        List<DemandeConge> findHistoriqueDirecteur(@Param("directeur") Utilisateur directeur);

        List<DemandeConge> findByLuParRHFalse();

        List<DemandeConge> findByDemandeurRoleAndLuParRHFalse(String role);

        @Query("SELECT d FROM DemandeConge d " +
                        "WHERE (:role IS NULL OR d.demandeur.role = :role) " +
                        "AND (:matricule IS NULL OR d.demandeur.matricule LIKE %:matricule%)")
        Page<DemandeConge> findByRoleAndMatricule(@Param("role") String role,
                        @Param("matricule") String matricule,
                        Pageable pageable);

        // Dans DemandeCongeRepository
        int countByStatutAndDemandeur_Departement(StatutDemande statut, Departement departement);

        List<DemandeConge> findByDemandeur_Departement(Departement departement);

        Page<DemandeConge> findByDemandeur_Departement(Departement departement, Pageable pageable);

        Page<DemandeConge> findByDemandeur_DepartementAndDemandeur_MatriculeContainsAndDemandeur_RoleContains(
                        Departement departement, String matricule, String role, Pageable pageable);

        List<DemandeConge> findByStatutAndDemandeur_Departement(StatutDemande statut, Departement departement);

        List<DemandeConge> findByStatutAndDemandeur(StatutDemande statut, Utilisateur demandeur);

        @Query("SELECT d FROM DemandeConge d WHERE d.demandeur = :demandeur AND d.statut = :statut AND YEAR(d.dateDebut) = :annee")
        List<DemandeConge> findByDemandeurAndStatutAndAnnee(@Param("demandeur") Utilisateur demandeur,
                        @Param("statut") StatutDemande statut,
                        @Param("annee") int annee);

        int countByDemandeurAndStatut(Utilisateur demandeur, StatutDemande statut);

        @Query("SELECT d FROM DemandeConge d WHERE d.demandeur = :directeur AND d.statut = 'APPROUVEE_DIRECTEUR' AND YEAR(d.dateDebut) = :annee")
        List<DemandeConge> findCongesApprouvesParDirecteur(@Param("directeur") Utilisateur directeur,
                        @Param("annee") int annee);

        @Query("SELECT d FROM DemandeConge d WHERE d.demandeur = :demandeur AND d.statut = :statut AND FUNCTION('YEAR', d.dateDebut) = :annee")
        List<DemandeConge> findApprovedByDemandeurAndYear(@Param("demandeur") Utilisateur demandeur,
                        @Param("statut") StatutDemande statut,
                        @Param("annee") int annee);

        @Query("SELECT d FROM DemandeConge d " +
                        "WHERE d.statut = :statut " +
                        "AND FUNCTION('YEAR', d.dateDebut) = :annee")
        List<DemandeConge> findApprovedByStatusAndYear(@Param("statut") StatutDemande statut,
                        @Param("annee") int annee);

        @Query("SELECT d FROM DemandeConge d WHERE d.demandeur = :demandeur " +
                        "AND d.statut = :statut " +
                        "AND YEAR(d.dateDebut) = :annee")
        List<DemandeConge> findByDemandeurAndStatutAndYear(@Param("demandeur") Utilisateur demandeur,
                        @Param("statut") StatutDemande statut,
                        @Param("annee") int annee);

        List<DemandeConge> findByDemandeur_DepartementAndDemandeur_RoleAndStatutIn(
                        Departement departement,
                        String role,
                        List<StatutDemande> statuts);

        List<DemandeConge> findByResponsableAndStatut(Utilisateur responsable, StatutDemande statut);

        // Récupérer les demandes pour lesquelles l'utilisateur est intérimaire et qui
        // sont en attente
        List<DemandeConge> findByInterimaireAndStatut(Utilisateur interimaire, StatutDemande statut);

        // Récupérer aussi les demandes déléguées à un intérimaire
        List<DemandeConge> findByInterimaire(Utilisateur interimaire);

        List<Utilisateur> findByDepartement(Departement departement);

        // Pour récupérer toutes les demandes dont l'utilisateur est responsable
        List<DemandeConge> findByResponsable(Utilisateur responsable);

        List<DemandeConge> findAllByStatut(String statut);

        List<DemandeConge> findAllByStatut(StatutDemande statut);

        List<DemandeConge> findByStatutAndDemandeurDepartementId(StatutDemande statut, Long depId);

        List<DemandeConge> findByDemandeur_DepartementAndDemandeur_Role(
                        Departement departement, String role);

        // 1. Pour récupérer les demandes approuvées hors département RH (id = 1)
        @Query("SELECT d FROM DemandeConge d WHERE d.statut = 'APPROUVEE_RESP' AND d.demandeur.departement.id <> 1")
        List<DemandeConge> findDemandesApprouveesHorsRH();

        // 2. Pour récupérer toutes les demandes du département RH (id = 1), tous
        // statuts
        @Query("SELECT d FROM DemandeConge d WHERE d.demandeur.departement.id = 1")
        List<DemandeConge> findDemandesDuDepartementRH();

        @Query("SELECT d FROM DemandeConge d " +
                        "WHERE (d.statut = 'APPROUVEE_RESPONSABLE') " +
                        "   OR (d.statut = 'EN_ATTENTE_RESPONSABLE' AND d.demandeur.departement.id = 1)")
        List<DemandeConge> findDemandesApprouveesEtEnAttenteRH();

        // Demandes approuvées par d'autres responsables (hors RH)
        @Query("SELECT d FROM DemandeConge d WHERE d.statut = 'APPROUVEE_RH' AND d.demandeur.role != 'RH'")
        List<DemandeConge> findDemandesApprouveesAutresResponsables();

        // Demandes en attente pour responsables, seulement pour le département 1
        @Query("SELECT d FROM DemandeConge d WHERE d.statut = 'EN_ATTENTE' AND d.demandeur.departement.id = 1")
        List<DemandeConge> findDemandesEnAttenteResponsableDepartement1();

}
