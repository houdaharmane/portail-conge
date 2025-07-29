package com.portailconge.portail_conge.model;

import jakarta.persistence.*;

@Entity
@Table(name = "historique_validation")
public class HistoriqueValidation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "demande_id", referencedColumnName = "id")
    private DemandeConge demande;

    @Column(name = "statut")
    private String statut;

    // Constructeurs, getters, setters

    public HistoriqueValidation() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DemandeConge getDemande() {
        return demande;
    }

    public void setDemande(DemandeConge demande) {
        this.demande = demande;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }
}
