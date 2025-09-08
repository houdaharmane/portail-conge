package com.portailconge.portail_conge.controller;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import com.portailconge.portail_conge.model.DemandeConge;
import com.portailconge.portail_conge.model.PdfGenerator;
import com.portailconge.portail_conge.model.StatutDemande;
import com.portailconge.portail_conge.model.Utilisateur;
import com.portailconge.portail_conge.repository.DepartementRepository;
import com.portailconge.portail_conge.repository.DemandeCongeRepository;
import com.portailconge.portail_conge.repository.UtilisateurRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class DirecteurController {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private DepartementRepository departementRepository;

    @Autowired
    private DemandeCongeRepository demandeCongeRepository;

    @GetMapping("/dashboard-directeur")
    public String dashboardDirecteur(HttpSession session, Model model) {
        Utilisateur directeur = (Utilisateur) session.getAttribute("utilisateur");
        if (directeur == null || !"DIRECTEUR".equals(directeur.getRole())) {
            return "redirect:/login";
        }

        int congesAnnuels = 30; // nombre de jours annuels
        int annee = java.time.LocalDate.now().getYear();

        // Récupérer les demandes approuvées du directeur pour l'année en cours
        List<DemandeConge> demandesApprouvees = demandeCongeRepository
                .findByDemandeurAndStatutAndYear(directeur, StatutDemande.APPROUVEE, annee);

        // Calculer le nombre total de jours consommés
        long congesConsommes = demandesApprouvees.stream()
                .filter(d -> d.getDateDebut() != null && d.getDateFin() != null)
                .mapToLong(d -> ChronoUnit.DAYS.between(d.getDateDebut(), d.getDateFin()) + 1)
                .sum();

        long soldeTotal = congesAnnuels - congesConsommes;

        // Statistiques pour le graphique
        int totalDemandes = demandeCongeRepository.findByDemandeur(directeur).size();

        // Compter les demandes en attente pour ce directeur
        int demandesEnAttente = demandeCongeRepository
                .countByStatutIn(List.of(StatutDemande.EN_ATTENTE_DIRECTEUR));

        int demandesApprouveesCount = demandesApprouvees.size();

        long tauxAcceptation = totalDemandes == 0 ? 0
                : Math.round((double) demandesApprouveesCount / totalDemandes * 100);

        // Ajouter les attributs au modèle
        model.addAttribute("directeur", directeur);
        model.addAttribute("activePage", "dashboard");

        model.addAttribute("congesAnnuels", congesAnnuels);
        model.addAttribute("congesConsommes", congesConsommes);
        model.addAttribute("soldeTotal", soldeTotal);

        model.addAttribute("totalDemandes", totalDemandes);
        model.addAttribute("demandesEnAttente", demandesEnAttente == 0 ? 0 : demandesEnAttente);
        model.addAttribute("demandesApprouvees", demandesApprouveesCount);
        model.addAttribute("tauxAcceptation", tauxAcceptation);

        session.setAttribute("utilisateurConnecteId", directeur.getId());

        return "dashboard-directeur";
    }

    @GetMapping("/profil-directeur")
    public String afficherProfilDirecteur(HttpSession session, Model model) {
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (utilisateur == null || !"DIRECTEUR".equals(utilisateur.getRole())) {
            return "redirect:/login";
        }

        model.addAttribute("directeur", utilisateur);
        model.addAttribute("departements", departementRepository.findAll());

        return "profil-directeur";
    }

    @PostMapping("/profil-directeur/save")
    public String sauvegarderProfilDirecteur(
            @ModelAttribute Utilisateur directeur,
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam(name = "confirm_password", required = false) String confirmPassword,
            HttpSession session,
            Model model) throws IOException {

        // Récupération de l'ID du directeur depuis la session (Integer)
        Integer utilisateurId = (Integer) session.getAttribute("utilisateurConnecteId");
        if (utilisateurId == null) {
            return "redirect:/login";
        }

        // Récupération de l'utilisateur depuis la base
        Optional<Utilisateur> optUser = utilisateurRepository.findById(utilisateurId);
        if (optUser.isEmpty()) {
            return "redirect:/login";
        }

        Utilisateur utilisateurEnBase = optUser.get();

        // Mise à jour du mot de passe si présent
        if (directeur.getMotDePasse() != null && !directeur.getMotDePasse().isEmpty()) {
            if (!directeur.getMotDePasse().equals(confirmPassword)) {
                model.addAttribute("error", "Les mots de passe ne correspondent pas");
                model.addAttribute("directeur", utilisateurEnBase);
                model.addAttribute("departements", departementRepository.findAll());
                return "profil-directeur";
            }
            utilisateurEnBase.setMotDePasse(directeur.getMotDePasse());
        }

        // Mise à jour des infos personnelles
        utilisateurEnBase.setNom(directeur.getNom());
        utilisateurEnBase.setPrenom(directeur.getPrenom());
        utilisateurEnBase.setTelephone(directeur.getTelephone());
        utilisateurEnBase.setEmail(directeur.getEmail());
        utilisateurEnBase.setCin(directeur.getCin());

        // Mise à jour du département (Integer)
        if (directeur.getDepartement() != null && directeur.getDepartement().getId() != null) {
            Integer deptId = directeur.getDepartement().getId();
            departementRepository.findById(deptId)
                    .ifPresent(utilisateurEnBase::setDepartement);
        } else {
            utilisateurEnBase.setDepartement(null);
        }

        // Traitement de l'image
        if (imageFile != null && !imageFile.isEmpty()) {
            utilisateurEnBase.setPhoto(imageFile.getBytes());
        }

        // Sauvegarde finale
        utilisateurRepository.save(utilisateurEnBase);
        session.setAttribute("utilisateur", utilisateurEnBase);

        return "redirect:/profil-directeur";
    }

    // ======================= DEMANDES =========================

    @GetMapping("/demandes-finales")
    public String afficherDemandesFinales(Model model, HttpSession session) {
        Utilisateur directeur = (Utilisateur) session.getAttribute("utilisateur");
        if (directeur == null || !"DIRECTEUR".equals(directeur.getRole())) {
            return "redirect:/login";
        }

        // Inclure uniquement les demandes EN_ATTENTE_DIRECTEUR
        List<DemandeConge> demandes = demandeCongeRepository.findByStatut(StatutDemande.EN_ATTENTE_DIRECTEUR);

        model.addAttribute("demandesFinales", demandes);
        return "demandes-finales";
    }

    @GetMapping("/directeur/accepter/{id}")
    public String accepterParDirecteur(@PathVariable Long id, HttpSession session) {
        Utilisateur directeur = (Utilisateur) session.getAttribute("utilisateur");
        if (directeur == null || !"DIRECTEUR".equals(directeur.getRole())) {
            return "redirect:/login";
        }

        demandeCongeRepository.findById(id).ifPresent(demande -> {
            if (demande.getStatut() == StatutDemande.EN_ATTENTE_DIRECTEUR) {
                demande.setStatut(StatutDemande.APPROUVEE_DIRECTEUR);

                try {
                    byte[] pdfBytes = PdfGenerator.generateCongePdf(demande, "Titre de Congé");

                    // Définir un nom de fichier unique
                    String nomFichier = "Titre_Conge_" + demande.getId() + ".pdf";
                    demande.setTitreConge(nomFichier);

                    // Sauvegarder dans un dossier local "conges"
                    java.nio.file.Path path = java.nio.file.Paths.get("./conges/");
                    if (!java.nio.file.Files.exists(path)) {
                        java.nio.file.Files.createDirectories(path);
                    }
                    java.nio.file.Files.write(path.resolve(nomFichier), pdfBytes);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                demandeCongeRepository.save(demande);
            }
        });

        return "redirect:/demandes-finales";
    }

    @GetMapping("/directeur/refuser/{id}")
    public String refuserParDirecteur(@PathVariable Long id, HttpSession session) {
        Utilisateur directeur = (Utilisateur) session.getAttribute("utilisateur");
        if (directeur == null || !"DIRECTEUR".equals(directeur.getRole())) {
            return "redirect:/login";
        }

        demandeCongeRepository.findById(id).ifPresent(demande -> {
            if (demande.getStatut() == StatutDemande.EN_ATTENTE_DIRECTEUR) {
                demande.setStatut(StatutDemande.REFUSEE_DIRECTEUR); // refusée par le directeur
                demandeCongeRepository.save(demande);
            }
        });

        return "redirect:/demandes-finales";
    }

    // FORMULAIRE DIRECTEUR

    @GetMapping("/directeur/demande-conge")
    public String afficherFormulaireDemandeConge(Model model, HttpSession session) {
        Utilisateur directeur = (Utilisateur) session.getAttribute("utilisateur");
        if (directeur == null || !"DIRECTEUR".equals(directeur.getRole())) {
            return "redirect:/login";
        }
        model.addAttribute("directeur", directeur);
        model.addAttribute("nomPrenom", directeur.getNom() + " " + directeur.getPrenom());
        model.addAttribute("matricule", directeur.getMatricule());
        model.addAttribute("role", directeur.getRole());

        model.addAttribute("activePage", "demandeConge");
        model.addAttribute("demandeConge", new DemandeConge());
        model.addAttribute("directeur", directeur);
        return "demande-conge-directeur";
    }

    @PostMapping("/directeur/demande-conge")
    public String soumettreDemandeConge(@ModelAttribute DemandeConge demandeConge, HttpSession session) {
        Utilisateur directeur = (Utilisateur) session.getAttribute("utilisateur");
        if (directeur == null || !"DIRECTEUR".equals(directeur.getRole())) {
            return "redirect:/login";
        }

        demandeConge.setDemandeur(directeur);

        // Par défaut, demande non lue par RH
        demandeConge.setLuParRH(false);

        // Statut peut rester EN_ATTENTE (au lieu de APPROUVEE directement)
        demandeConge.setStatut(StatutDemande.APPROUVEE);

        demandeCongeRepository.save(demandeConge);

        return "ConfirmationDemande";
    }

    @GetMapping("/directeur/historique")
    public String historiqueDirecteur(HttpSession session, Model model) {
        Utilisateur directeur = (Utilisateur) session.getAttribute("utilisateur");
        if (directeur == null || !"DIRECTEUR".equals(directeur.getRole())) {
            return "redirect:/login";
        }

        List<DemandeConge> demandes = demandeCongeRepository.findHistoriqueDirecteur(directeur);

        model.addAttribute("demandes", demandes);
        model.addAttribute("directeur", directeur);
        model.addAttribute("activePage", "historique");

        return "historique-directeur";
    }

    @GetMapping("/conge/download/{id}")
    public ResponseEntity<byte[]> telechargerPdf(@PathVariable Long id) throws Exception {
        DemandeConge demande = demandeCongeRepository.findById(id).orElse(null);
        if (demande == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] pdfBytes = PdfGenerator.generateCongePdf(demande, "Titre de Congé");

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"Titre_Conge_" + demande.getId() + ".pdf\"")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    // Télécharger la fiche de congé (PDF) pour directeur
    @GetMapping("/conge/download/fiche/{id}")
    public ResponseEntity<byte[]> telechargerFicheConge(@PathVariable Long id, HttpSession session) {
        DemandeConge demande = demandeCongeRepository.findById(id).orElse(null);
        if (demande == null) {
            return ResponseEntity.notFound().build();
        }

        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (utilisateur == null) {
            return ResponseEntity.status(403).build();
        }

        boolean estDemandeur = demande.getDemandeur().getId() == utilisateur.getId();
        boolean estDirecteur = "DIRECTEUR".equals(utilisateur.getRole());
        boolean estApprouvee = demande.getStatut() == StatutDemande.APPROUVEE_DIRECTEUR;

        if (!estDemandeur && !(estDirecteur && estApprouvee)) {
            return ResponseEntity.status(403).build();
        }

        byte[] pdfBytes;
        try {
            String titrePdf = estDirecteur ? "Fiche de congé Directeur" : "Fiche de congé";
            pdfBytes = PdfGenerator.generateCongePdf(demande, titrePdf);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur lors de la génération du PDF".getBytes());
        }

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"Fiche_Conge_" + demande.getId() + ".pdf\"")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    // Télécharger le titre de congé (PDF) pour directeur
    @GetMapping("/conge/download/titre/{id}")
    public ResponseEntity<byte[]> telechargerTitreConge(@PathVariable Long id, HttpSession session) {
        DemandeConge demande = demandeCongeRepository.findById(id).orElse(null);
        System.out.println("Demande : " + demande);
        System.out.println("Debut : " + demande.getDateDebut());
        System.out.println("Fin : " + demande.getDateFin());
        System.out.println("Demandeur : " + demande.getDemandeur());
        System.out.println("Titre : " + demande.getTitreConge());

        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (utilisateur == null) {
            return ResponseEntity.status(403).build();
        }

        boolean estDemandeur = demande.getDemandeur().getId() == utilisateur.getId();
        boolean estDirecteur = "DIRECTEUR".equals(utilisateur.getRole());
        boolean estApprouvee = demande.getStatut() == StatutDemande.APPROUVEE_DIRECTEUR;

        // Autorisation : demandeur ou directeur si la demande est approuvée
        if (!estDemandeur && !(estDirecteur && estApprouvee)) {
            return ResponseEntity.status(403).build();
        }

        // Vérifier que les champs nécessaires sont présents
        if (demande.getDemandeur() == null || demande.getDateDebut() == null || demande.getDateFin() == null) {
            return ResponseEntity.status(500).body("Impossible de générer le PDF : données manquantes".getBytes());
        }

        byte[] pdfBytes;
        try {
            String titrePdf = estDirecteur ? "Titre de Congé Directeur" : "Titre de Congé";
            pdfBytes = PdfGenerator.generateCongePdf(demande, titrePdf);
            if (pdfBytes == null || pdfBytes.length == 0) {
                return ResponseEntity.status(500).body("Erreur : PDF vide".getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur lors de la génération du PDF".getBytes());
        }

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"Titre_Conge_" + demande.getId() + ".pdf\"")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

}
