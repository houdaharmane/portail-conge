package com.portailconge.portail_conge.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Transient;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class DemandeConge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dateDebut;
    private LocalDate dateFin;
    private int duree; // Durée en jours

    @ManyToOne
    private Utilisateur demandeur;

    @ManyToOne
    private Departement departement;

    @Enumerated(EnumType.STRING)
    private StatutDemande statut;

    @Column(name = "lu_par_rh")
    private Boolean luParRH = false;

    @Column(name = "date_soumission")
    private LocalDateTime dateSoumission;

    private String fonction;
    private String lieuDate;

    @Enumerated(EnumType.STRING)
    private StatutFiche statutFiche;

    private String destinataireActuel;

    // Champs temporaires pour affichage formaté
    @Transient
    private String dateDebutFormatee;

    @Transient
    private String dateFinFormatee;

    @Transient
    private String dateSoumissionFormatee;
    private String titreConge;

    public String getTitreConge() {
        return titreConge;
    }

    public void setTitreConge(String titreConge) {
        this.titreConge = titreConge;
    }

    // Champ temporaire pour afficher le bouton "Télécharger Titre"
    @Transient
    private boolean titreVisible;

    public boolean isTitreVisible() {
        return titreVisible;
    }

    public void setTitreVisible(boolean titreVisible) {
        this.titreVisible = titreVisible;
    }

    // -------------------- Enum --------------------
    public enum StatutFiche {
        EN_ATTENTE_RESPONSABLE,
        SIGNE_RESPONSABLE,
        EN_ATTENTE_DIRECTEUR,
        SIGNE_DIRECTEUR
    }

    // -------------------- Constructeurs --------------------
    public DemandeConge() {
    }

    // -------------------- Getters et Setters --------------------
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public int getDuree() {
        return duree;
    }

    public void setDuree(int duree) {
        this.duree = duree;
    }

    public Utilisateur getDemandeur() {
        return demandeur;
    }

    public void setDemandeur(Utilisateur demandeur) {
        this.demandeur = demandeur;
    }

    public Departement getDepartement() {
        return departement;
    }

    public void setDepartement(Departement departement) {
        this.departement = departement;
    }

    public StatutDemande getStatut() {
        return statut;
    }

    public void setStatut(StatutDemande statut) {
        this.statut = statut;
    }

    public Boolean getLuParRH() {
        return luParRH;
    }

    public void setLuParRH(Boolean luParRH) {
        this.luParRH = luParRH;
    }

    public LocalDateTime getDateSoumission() {
        return dateSoumission;
    }

    public void setDateSoumission(LocalDateTime dateSoumission) {
        this.dateSoumission = dateSoumission;
    }

    public String getFonction() {
        return fonction;
    }

    public void setFonction(String fonction) {
        this.fonction = fonction;
    }

    public String getLieuDate() {
        return lieuDate;
    }

    public void setLieuDate(String lieuDate) {
        this.lieuDate = lieuDate;
    }

    public StatutFiche getStatutFiche() {
        return statutFiche;
    }

    public void setStatutFiche(StatutFiche statutFiche) {
        this.statutFiche = statutFiche;
    }

    public String getDestinataireActuel() {
        return destinataireActuel;
    }

    public void setDestinataireActuel(String destinataireActuel) {
        this.destinataireActuel = destinataireActuel;
    }

    public String getDateDebutFormatee() {
        return dateDebutFormatee;
    }

    public void setDateDebutFormatee(String dateDebutFormatee) {
        this.dateDebutFormatee = dateDebutFormatee;
    }

    public String getDateFinFormatee() {
        return dateFinFormatee;
    }

    public void setDateFinFormatee(String dateFinFormatee) {
        this.dateFinFormatee = dateFinFormatee;
    }

    public String getDateSoumissionFormatee() {
        return dateSoumissionFormatee;
    }

    public void setDateSoumissionFormatee(String dateSoumissionFormatee) {
        this.dateSoumissionFormatee = dateSoumissionFormatee;
    }
}
