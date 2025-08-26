package com.portailconge.portail_conge.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Transient;

@Entity
public class DemandeConge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dateDebut;
    private LocalDate dateFin;
    private int duree; // durée en jours

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

    // Champs temporaires pour l'affichage formaté
    @Transient
    private String dateDebutFormatee;

    @Transient
    private String dateFinFormatee;

    @Transient
    private String dateSoumissionFormatee;

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