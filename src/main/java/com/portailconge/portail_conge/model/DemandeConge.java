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
import jakarta.persistence.Transient;

@Entity
public class DemandeConge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate dateFin;
    private LocalDate dateDebut;
    private int duree; // durée en jours

    @ManyToOne
    private Utilisateur demandeur; // le personnel qui fait la demande
    @Enumerated(EnumType.STRING)
    private StatutDemande statut;

    public DemandeConge() {
    }

    @ManyToOne
    private Departement departement;

    public Departement getDepartement() {
        return departement;
    }

    public void setDepartement(Departement departement) {
        this.departement = departement;
    }

    @Transient
    private String dateDebutFormatee;

    @Transient
    private String dateFinFormatee;

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

    @Transient
    private String dateSoumissionFormatee;

    public String getDateSoumissionFormatee() {
        return dateSoumissionFormatee;
    }

    public void setDateSoumissionFormatee(String dateSoumissionFormatee) {
        this.dateSoumissionFormatee = dateSoumissionFormatee;
    }
    // Getters et setters

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

    public StatutDemande getStatut() {
        return statut;
    }

    public void setStatut(StatutDemande statut) {
        this.statut = statut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    @Column(name = "date_soumission")
    private LocalDate dateSoumission;

    public LocalDate getDateSoumission() {
        return dateSoumission;
    }

    public void setDateSoumission(LocalDate dateSoumission) {
        this.dateSoumission = dateSoumission;
    }

}
