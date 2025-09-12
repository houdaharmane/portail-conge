package com.portailconge.portail_conge.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "utilisateur")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Lob
    @Column(name = "photo", columnDefinition = "LONGBLOB")
    private byte[] photo;

    private String photoUrl;
    private String signatureImagePath;
    private Boolean responsableRh = false;

    private String matricule;
    private String nom;
    private String prenom;

    @Column(unique = true)
    private String email;

    private String telephone;

    @Column(name = "mot_de_passe")
    private String motDePasse;

    private String cin;
    private Integer soldeConge;

    @ManyToOne
    @JoinColumn(name = "departement_id")
    private Departement departement;

    private String role;

    // ===== Relations avec d'autres utilisateurs =====
    @ManyToOne
    @JoinColumn(name = "interimaire_id")
    private Utilisateur interimaire; // utilisateur intérimaire

    private LocalDate debutInterim;
    private LocalDate finInterim;

    // ===== Relations avec les demandes de congé =====
    @OneToMany(mappedBy = "demandeur", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<DemandeConge> demandesConge;

    @OneToMany(mappedBy = "responsable")
    private List<DemandeConge> demandesResponsable;

    @OneToMany(mappedBy = "interimaire")
    private List<DemandeConge> demandesInterimaire;

    // ===== Relations avec le personnel (si tu as une table Personnel) =====
    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Personnel> personnels;

    // ===== Getters et Setters =====

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getSignatureImagePath() {
        return signatureImagePath;
    }

    public void setSignatureImagePath(String signatureImagePath) {
        this.signatureImagePath = signatureImagePath;
    }

    public Boolean getResponsableRh() {
        return responsableRh;
    }

    public void setResponsableRh(Boolean responsableRh) {
        this.responsableRh = responsableRh;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public Integer getSoldeConge() {
        return soldeConge;
    }

    public void setSoldeConge(Integer soldeConge) {
        this.soldeConge = soldeConge;
    }

    public Departement getDepartement() {
        return departement;
    }

    public void setDepartement(Departement departement) {
        this.departement = departement;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Utilisateur getInterimaire() {
        return interimaire;
    }

    public void setInterimaire(Utilisateur interimaire) {
        this.interimaire = interimaire;
    }

    public LocalDate getDebutInterim() {
        return debutInterim;
    }

    public void setDebutInterim(LocalDate debutInterim) {
        this.debutInterim = debutInterim;
    }

    public LocalDate getFinInterim() {
        return finInterim;
    }

    public void setFinInterim(LocalDate finInterim) {
        this.finInterim = finInterim;
    }

    public List<DemandeConge> getDemandesConge() {
        return demandesConge;
    }

    public void setDemandesConge(List<DemandeConge> demandesConge) {
        this.demandesConge = demandesConge;
    }

    public List<DemandeConge> getDemandesResponsable() {
        return demandesResponsable;
    }

    public void setDemandesResponsable(List<DemandeConge> demandesResponsable) {
        this.demandesResponsable = demandesResponsable;
    }

    public List<DemandeConge> getDemandesInterimaire() {
        return demandesInterimaire;
    }

    public void setDemandesInterimaire(List<DemandeConge> demandesInterimaire) {
        this.demandesInterimaire = demandesInterimaire;
    }

    public List<Personnel> getPersonnels() {
        return personnels;
    }

    public void setPersonnels(List<Personnel> personnels) {
        this.personnels = personnels;
    }

    // ===== Enum pour les rôles =====
    public enum Role {
        PERSONNEL, RESPONSABLE, RH, DIRECTEUR, ADMISSION
    }
}
