package com.portailconge.portail_conge.controller;

import java.io.IOException;
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
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (utilisateur == null || !"DIRECTEUR".equals(utilisateur.getRole())) {
            return "redirect:/login";
        }

        // Statistiques dynamiques
        long totalDemandes = demandeCongeRepository.count();
        long demandesEnAttente = demandeCongeRepository.countByStatut(StatutDemande.EN_ATTENTE_DIRECTEUR);
        long demandesApprouvees = demandeCongeRepository.countByStatut(StatutDemande.APPROUVEE_DIRECTEUR);
        long tauxAcceptation = totalDemandes == 0 ? 0 : Math.round((double) demandesApprouvees / totalDemandes * 100);

        model.addAttribute("directeur", utilisateur);
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("totalDemandes", totalDemandes);
        model.addAttribute("demandesEnAttente", demandesEnAttente);
        model.addAttribute("demandesApprouvees", demandesApprouvees);
        model.addAttribute("tauxAcceptation", tauxAcceptation);

        session.setAttribute("utilisateurConnecteId", utilisateur.getId());
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

}
