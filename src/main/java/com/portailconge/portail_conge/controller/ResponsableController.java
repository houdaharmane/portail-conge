package com.portailconge.portail_conge.controller;

import com.portailconge.portail_conge.model.DemandeConge;
import com.portailconge.portail_conge.model.Departement;
import com.portailconge.portail_conge.model.StatutDemande;
import com.portailconge.portail_conge.model.Utilisateur;
import com.portailconge.portail_conge.repository.DemandeCongeRepository;
import com.portailconge.portail_conge.service.CongeService;
import com.portailconge.portail_conge.service.UtilisateurService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Controller
public class ResponsableController {

    @Autowired
    private CongeService congeService;

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private DemandeCongeRepository demandeCongeRepository;

    // Dashboard responsable
    @GetMapping("/dashboard-responsable")
    public String dashboardResponsable(HttpSession session, Model model) {
        Utilisateur responsable = (Utilisateur) session.getAttribute("utilisateur");
        if (responsable == null) {
            return "redirect:/login";
        }

        if (!("RESPONSABLE".equals(responsable.getRole()) || "PERSONNEL".equals(responsable.getRole()))) {
            return "redirect:/login";
        }

        int nombrePersonnels = utilisateurService.getNombrePersonnels();
        int congesEnAttente = congeService.getCongesEnAttente();
        int congesValides = congeService.getCongesValides();
        int congesRefuses = congeService.getCongesRefuses();

        model.addAttribute("totalPersonnel", nombrePersonnels);
        model.addAttribute("congesEnAttente", congesEnAttente);
        model.addAttribute("congesValides", congesValides);
        model.addAttribute("utilisateur", responsable);
        model.addAttribute("congesRefuses", congesRefuses);

        return "dashboard-responsable";
    }

    // Soumission demande (personnel)
    @PostMapping("/conge/demande/soumettre")
    public String soumettreDemande(@ModelAttribute DemandeConge demandeConge, HttpSession session) {
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (utilisateur == null) {
            return "redirect:/login";
        }

        demandeConge.setDemandeur(utilisateur);
        demandeConge.setStatut(StatutDemande.EN_ATTENTE);
        demandeConge.setDateSoumission(LocalDate.now());
        demandeCongeRepository.save(demandeConge);

        return "redirect:/conges/confirmation";
    }

    @GetMapping("/demandes-a-traiter")
    public String afficherDemandesATraiter(HttpSession session, Model model) {
        Utilisateur responsable = (Utilisateur) session.getAttribute("utilisateur");
        if (responsable == null || !"RESPONSABLE".equals(responsable.getRole())) {
            return "redirect:/login";
        }

        Departement departement = responsable.getDepartement();

        // Récupère uniquement les demandes EN_ATTENTE des personnels de ce département
        List<DemandeConge> demandes = demandeCongeRepository.findDemandesEnAttentePersonnelByDepartement(
                departement, StatutDemande.EN_ATTENTE_RESPONSABLE);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (DemandeConge demande : demandes) {
            if (demande.getDateDebut() != null) {
                demande.setDateDebutFormatee(demande.getDateDebut().format(formatter));
            }
            if (demande.getDateFin() != null) {
                demande.setDateFinFormatee(demande.getDateFin().format(formatter));
            }
        }

        model.addAttribute("demandes", demandes);
        model.addAttribute("activePage", "demandesATraiter");

        return "demandesATraiter";
    }

    // Approuver demande responsable → statut APPROUVEE_RESP
    @GetMapping("/approuver/{id}")
    public String approuverDemande(@PathVariable Long id, HttpSession session) {
        Utilisateur responsable = (Utilisateur) session.getAttribute("utilisateur");
        if (responsable == null || !"RESPONSABLE".equals(responsable.getRole())) {
            return "redirect:/login";
        }

        DemandeConge demande = demandeCongeRepository.findById(id).orElse(null);
        if (demande != null) {
            demande.setStatut(StatutDemande.APPROUVEE_RESP);
            demandeCongeRepository.save(demande);
        }
        return "redirect:/demandes-a-traiter";
    }

    // Refuser demande responsable → statut REFUSEE_RESP
    @GetMapping("/rejeter/{id}")
    public String rejeterDemande(@PathVariable Long id, HttpSession session) {
        Utilisateur responsable = (Utilisateur) session.getAttribute("utilisateur");
        if (responsable == null || !"RESPONSABLE".equals(responsable.getRole())) {
            return "redirect:/login";
        }

        DemandeConge demande = demandeCongeRepository.findById(id).orElse(null);
        if (demande != null) {
            demande.setStatut(StatutDemande.REFUSEE_RESP);
            demandeCongeRepository.save(demande);
        }
        return "redirect:/demandes-a-traiter";
    }

    // Afficher formulaire demande congé responsable
    @GetMapping("/responsable/conge/demande")
    public String afficherFormulaireDemande(Model model, HttpSession session) {
        Utilisateur responsable = (Utilisateur) session.getAttribute("utilisateur");
        if (responsable == null || !"RESPONSABLE".equals(responsable.getRole())) {
            return "redirect:/login";
        }

        model.addAttribute("matricule", responsable.getMatricule());
        model.addAttribute("nomPrenom", responsable.getNom() + " " + responsable.getPrenom());
        model.addAttribute("departementId", responsable.getDepartement().getId());
        model.addAttribute("role", responsable.getRole());

        return "demandeConge-responsable";
    }

    @PostMapping("/responsable/conge/demande/soumettre")
    public String soumettreDemandeCongeResponsable(@ModelAttribute DemandeConge demandeConge,
            HttpSession session,
            Model model) {
        Utilisateur responsable = (Utilisateur) session.getAttribute("utilisateur");
        if (responsable == null || !"RESPONSABLE".equals(responsable.getRole())) {
            return "redirect:/login";
        }

        demandeConge.setDemandeur(responsable);
        demandeConge.setStatut(StatutDemande.EN_ATTENTE);
        demandeConge.setDateSoumission(LocalDate.now());
        demandeCongeRepository.save(demandeConge);

        // Définir dynamiquement l'URL du dashboard selon le rôle ou l'utilisateur
        String dashboardUrl;
        if ("RESPONSABLE".equals(responsable.getRole())) {
            dashboardUrl = "/dashboard-responsable";
        } else if ("PERSONNEL".equals(responsable.getRole())) {
            dashboardUrl = "/personnel/dashboard";
        } else {
            dashboardUrl = "/dashboard";
        }

        model.addAttribute("dashboardUrl", dashboardUrl);

        return "ConfirmationDemande";
    }

    // Afficher demandes approuvées par responsable pour ce département (statut
    // APPROUVEE_RESP)
    // À noter : cette page est plutôt dédiée au RH, il serait mieux de créer un
    // RhController
    // Mais on garde ici pour l'instant, à adapter selon ta structure
    @GetMapping("/demandes-approuvees")
    public String afficherDemandesApprouvees(HttpSession session, Model model) {
        Utilisateur responsable = (Utilisateur) session.getAttribute("utilisateur");

        if (responsable == null || !"RESPONSABLE".equals(responsable.getRole())) {
            return "redirect:/login";
        }

        Departement departement = responsable.getDepartement();

        List<DemandeConge> demandesApprouvees = demandeCongeRepository.findDemandesByDepartementAndStatut(
                departement, StatutDemande.APPROUVEE_RESP);
        model.addAttribute("demandes", demandesApprouvees);
        model.addAttribute("activePage", "demandesApprouvees");
        // Vue différente de RH si tu veux : "demandesApprouveesResponsable"
        return "demandesApprouveesResponsable";
    }

    // Afficher profil responsable
    @GetMapping("/profil-responsable")
    public String afficherProfil(HttpSession session, Model model) {
        Utilisateur responsable = (Utilisateur) session.getAttribute("utilisateur");
        if (responsable == null) {
            return "redirect:/login";
        }
        model.addAttribute("utilisateur", responsable);
        return "profil-responsable";
    }

    private void formaterDatesDemandes(Page<DemandeConge> demandesPage) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (DemandeConge demande : demandesPage.getContent()) {
            if (demande.getDateSoumission() != null) {
                demande.setDateSoumissionFormatee(demande.getDateSoumission().format(formatter));
            }
            if (demande.getDateDebut() != null) {
                demande.setDateDebutFormatee(demande.getDateDebut().format(formatter));
            }
            if (demande.getDateFin() != null) {
                demande.setDateFinFormatee(demande.getDateFin().format(formatter));
            }
        }
    }

    @GetMapping("/responsable/historique-demandes-responsable")
    public String afficherHistoriqueDemandesResponsable(
            HttpSession session,
            Model model,
            @RequestParam(defaultValue = "0") int page) {

        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (utilisateur == null || !"RESPONSABLE".equals(utilisateur.getRole())) {
            return "redirect:/login";
        }

        int pageSize = 3;
        Pageable pageable = PageRequest.of(page, pageSize);

        // Inclure tous les statuts que tu veux voir dans l’historique
        List<StatutDemande> statutsHistorique = List.of(
                StatutDemande.EN_ATTENTE,
                StatutDemande.EN_ATTENTE_RESPONSABLE,
                StatutDemande.APPROUVEE_RESP,
                StatutDemande.REFUSEE_RESP,
                StatutDemande.EN_ATTENTE_RH,
                StatutDemande.APPROUVEE_RH,
                StatutDemande.REFUSEE_RH,
                StatutDemande.EN_ATTENTE_DIRECTEUR,
                StatutDemande.APPROUVEE_DIRECTEUR,
                StatutDemande.REFUSEE_DIRECTEUR);

        Page<DemandeConge> demandesPage = demandeCongeRepository.findByDemandeurAndStatutIn(utilisateur,
                statutsHistorique, pageable);

        formaterDatesDemandes(demandesPage);

        model.addAttribute("demandes", demandesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", demandesPage.getTotalPages());
        model.addAttribute("activePage", "historiqueDemandesResponsable");

        return "HistoriqueDemandes-responsable";
    }

    // Afficher historique des demandes faites par le personnel
    @GetMapping("/responsable/historique-demandes-personnel")
    public String afficherHistoriqueDemandesPersonnel(
            HttpSession session,
            Model model,
            @RequestParam(defaultValue = "0") int page) {
        int pageSize = 3;
        Pageable pageable = PageRequest.of(page, pageSize);
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (utilisateur == null || !"RESPONSABLE".equals(utilisateur.getRole())) {
            return "redirect:/login";
        }

        Page<DemandeConge> demandesPage = demandeCongeRepository.findByDemandeurRole("PERSONNEL", pageable);

        formaterDatesDemandes(demandesPage);

        model.addAttribute("demandes", demandesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", demandesPage.getTotalPages());
        model.addAttribute("activePage", "historiqueDemandesPersonnel");

        return "historique-demandes-responsable";
    }

}
