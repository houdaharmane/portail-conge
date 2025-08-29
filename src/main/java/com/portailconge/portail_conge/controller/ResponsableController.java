package com.portailconge.portail_conge.controller;

import com.portailconge.portail_conge.model.*;
import com.portailconge.portail_conge.repository.DemandeCongeRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class ResponsableController {

    @Autowired
    private DemandeCongeRepository demandeCongeRepository;

    @Autowired
    private CongePdfGenerator congePdfGenerator;

    // ---------------- Dashboard ----------------
    @GetMapping("/dashboard-responsable")
    public String dashboardResponsable(HttpSession session, Model model) {
        Utilisateur responsable = (Utilisateur) session.getAttribute("utilisateur");
        if (responsable == null)
            return "redirect:/login";
        if (!"RESPONSABLE".equals(responsable.getRole()) && !"PERSONNEL".equals(responsable.getRole()))
            return "redirect:/login";

        // Comptage des congés pour le graphique
        int congesEnAttente = demandeCongeRepository
                .countByStatutAndDemandeur_Departement(StatutDemande.EN_ATTENTE_RESPONSABLE,
                        responsable.getDepartement());
        int congesValides = demandeCongeRepository
                .countByStatutAndDemandeur_Departement(StatutDemande.APPROUVEE_RESP,
                        responsable.getDepartement());
        int congesRefuses = demandeCongeRepository
                .countByStatutAndDemandeur_Departement(StatutDemande.REFUSEE_RESP,
                        responsable.getDepartement());

        model.addAttribute("congesEnAttente", congesEnAttente);
        model.addAttribute("congesValides", congesValides);
        model.addAttribute("congesRefuses", congesRefuses);
        model.addAttribute("utilisateur", responsable);

        // ----------------- LOGIQUE DES SOLDES -----------------
        // Récupérer toutes les demandes validées pour le département
        List<DemandeConge> congesValidesUtilisateur = demandeCongeRepository
                .findByStatutAndDemandeur(StatutDemande.APPROUVEE_DIRECTEUR, responsable);

        int soldeTotal = 0; // Total de jours accordés (peut venir d'une configuration par rôle)
        int soldeConsomme = 0; // Somme des jours déjà pris
        int soldeDisponible = 0; // soldeTotal - soldeConsomme

        // Supposons que chaque utilisateur a un solde annuel de 24 jours
        soldeTotal = 30;

        // Calcul du solde consommé
        for (DemandeConge demande : congesValidesUtilisateur) {
            if (demande.getDateDebut() != null && demande.getDateFin() != null) {
                int jours = (int) java.time.temporal.ChronoUnit.DAYS.between(
                        demande.getDateDebut(), demande.getDateFin()) + 1;
                if (jours > 0)
                    soldeConsomme += jours;
            }
        }

        soldeDisponible = soldeTotal - soldeConsomme;

        model.addAttribute("soldeTotal", soldeTotal);
        model.addAttribute("soldeConsomme", soldeConsomme);
        model.addAttribute("soldeDisponible", soldeDisponible);
        // -------------------------------------------------------

        return "dashboard-responsable";
    }

    // ---------------- Soumission de demande ----------------
    @GetMapping("/responsable/conge/demande")
    public String afficherFormulaireDemande(Model model, HttpSession session) {
        Utilisateur responsable = (Utilisateur) session.getAttribute("utilisateur");
        if (responsable == null || !"RESPONSABLE".equals(responsable.getRole()))
            return "redirect:/login";

        model.addAttribute("matricule", responsable.getMatricule());
        model.addAttribute("nomPrenom", responsable.getNom() + " " + responsable.getPrenom());
        model.addAttribute("departementId", responsable.getDepartement().getId());
        model.addAttribute("role", responsable.getRole());

        return "demandeConge-responsable";
    }

    @PostMapping("/responsable/conge/demande/soumettre")
    public String soumettreDemandeCongeResponsable(@ModelAttribute DemandeConge demandeConge,
            HttpSession session, Model model) {
        Utilisateur responsable = (Utilisateur) session.getAttribute("utilisateur");
        if (responsable == null || !"RESPONSABLE".equals(responsable.getRole()))
            return "redirect:/login";

        demandeConge.setDemandeur(responsable);
        demandeConge.setStatut(StatutDemande.EN_ATTENTE);
        demandeConge.setDateSoumission(java.time.LocalDateTime.now());
        demandeCongeRepository.save(demandeConge);

        model.addAttribute("dashboardUrl", "/dashboard-responsable");
        return "ConfirmationDemande";
    }

    // ---------------- Afficher demandes à traiter ----------------
    @GetMapping("/demandes-a-traiter")
    public String afficherDemandesATraiter(HttpSession session, Model model) {
        Utilisateur responsable = (Utilisateur) session.getAttribute("utilisateur");
        if (responsable == null || !"RESPONSABLE".equals(responsable.getRole()))
            return "redirect:/login";

        Departement departement = responsable.getDepartement();
        List<DemandeConge> demandes = demandeCongeRepository
                .findDemandesEnAttentePersonnelByDepartement(departement, StatutDemande.EN_ATTENTE_RESPONSABLE);

        formaterDatesDemandes(demandes);
        model.addAttribute("demandes", demandes);
        model.addAttribute("activePage", "demandesATraiter");
        return "demandesATraiter";
    }

    @GetMapping("/approuver/{id}")
    public String approuverDemande(@PathVariable Long id, HttpSession session) {
        Utilisateur responsable = (Utilisateur) session.getAttribute("utilisateur");
        if (responsable == null || !"RESPONSABLE".equals(responsable.getRole()))
            return "redirect:/login";

        demandeCongeRepository.findById(id).ifPresent(d -> {
            d.setStatut(StatutDemande.APPROUVEE_RESP);
            demandeCongeRepository.save(d);
        });
        return "redirect:/demandes-a-traiter";
    }

    @GetMapping("/rejeter/{id}")
    public String rejeterDemande(@PathVariable Long id, HttpSession session) {
        Utilisateur responsable = (Utilisateur) session.getAttribute("utilisateur");
        if (responsable == null || !"RESPONSABLE".equals(responsable.getRole()))
            return "redirect:/login";

        demandeCongeRepository.findById(id).ifPresent(d -> {
            d.setStatut(StatutDemande.REFUSEE_RESP);
            demandeCongeRepository.save(d);
        });
        return "redirect:/demandes-a-traiter";
    }

    // ---------------- Historique des demandes ----------------
    @GetMapping("/responsable/historique-demandes-responsable")
    public String afficherHistoriqueDemandesResponsable(HttpSession session, Model model,
            @RequestParam(defaultValue = "0") int page) {
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (utilisateur == null || !"RESPONSABLE".equals(utilisateur.getRole()))
            return "redirect:/login";

        int pageSize = 9;
        Pageable pageable = PageRequest.of(page, pageSize);

        List<StatutDemande> statutsHistorique = List.of(
                StatutDemande.EN_ATTENTE, StatutDemande.EN_ATTENTE_RESPONSABLE,
                StatutDemande.APPROUVEE_RESP, StatutDemande.REFUSEE_RESP,
                StatutDemande.EN_ATTENTE_RH, StatutDemande.APPROUVEE_RH, StatutDemande.REFUSEE_RH,
                StatutDemande.EN_ATTENTE_DIRECTEUR, StatutDemande.APPROUVEE_DIRECTEUR, StatutDemande.REFUSEE_DIRECTEUR);

        Page<DemandeConge> demandesPage = demandeCongeRepository
                .findByDemandeurAndStatutIn(utilisateur, statutsHistorique, pageable);

        List<DemandeConge> demandes = demandesPage.getContent();
        for (DemandeConge d : demandes)
            d.setTitreVisible(d.getStatut() == StatutDemande.APPROUVEE_DIRECTEUR);

        formaterDatesDemandes(demandes);

        model.addAttribute("demandes", demandes);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", demandesPage.getTotalPages());
        model.addAttribute("activePage", "historiqueDemandesResponsable");
        return "HistoriqueDemandes-responsable";
    }

    @GetMapping("/responsable/historique-demandes-personnel")
    public String afficherHistoriqueDemandesPersonnel(HttpSession session, Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String matricule,
            @RequestParam(required = false) String role) {

        Utilisateur responsable = (Utilisateur) session.getAttribute("utilisateur");
        if (responsable == null || !"RESPONSABLE".equals(responsable.getRole()))
            return "redirect:/login";

        int pageSize = 3;
        Pageable pageable = PageRequest.of(page, pageSize);

        Page<DemandeConge> demandesPage;
        if ((matricule == null || matricule.isEmpty()) && (role == null || role.isEmpty())) {
            demandesPage = demandeCongeRepository.findByDemandeur_Departement(responsable.getDepartement(), pageable);
        } else {
            demandesPage = demandeCongeRepository
                    .findByDemandeur_DepartementAndDemandeur_MatriculeContainsAndDemandeur_RoleContains(
                            responsable.getDepartement(),
                            matricule != null ? matricule : "",
                            role != null ? role : "",
                            pageable);
        }

        List<DemandeConge> demandes = demandesPage.getContent();
        formaterDatesDemandes(demandes);

        model.addAttribute("demandes", demandes);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", demandesPage.getTotalPages());
        model.addAttribute("activePage", "historiqueDemandesPersonnel");
        model.addAttribute("matricule", matricule);
        model.addAttribute("role", role);

        return "historique-demandes-personnel";
    }

    // ---------------- Profil ----------------
    @GetMapping("/profil-responsable")
    public String afficherProfil(HttpSession session, Model model) {
        Utilisateur responsable = (Utilisateur) session.getAttribute("utilisateur");
        if (responsable == null)
            return "redirect:/login";

        model.addAttribute("utilisateur", responsable);
        return "profil-responsable";
    }

    // ---------------- Génération PDF ----------------
    @GetMapping("/demandes/telecharger-titre/{id}")
    public ResponseEntity<byte[]> telechargerTitre(@PathVariable Long id, HttpSession session) {
        Utilisateur responsable = (Utilisateur) session.getAttribute("utilisateur");
        if (responsable == null || !"RESPONSABLE".equals(responsable.getRole()))
            return ResponseEntity.status(403).build();

        DemandeConge demande = demandeCongeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée"));

        if (!demande.getDemandeur().getDepartement().getId()
                .equals(responsable.getDepartement().getId())) {
            return ResponseEntity.status(403).build();
        }

        try {
            String titrePdf = "Titre de congé";
            byte[] pdf = PdfGenerator.generateCongePdf(demande, titrePdf);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=TitreConge_" + demande.getId() + ".pdf")
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .body(pdf);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/personnel/demande/{id}/fiche-pdf")
    public ResponseEntity<byte[]> telechargerFichePdf(@PathVariable Long id, HttpSession session) {
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (utilisateur == null)
            return ResponseEntity.status(403).build();

        DemandeConge demande = demandeCongeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée"));

        try {
            byte[] pdf = congePdfGenerator.genererFichePdf(demande, utilisateur);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=FicheConge_" + demande.getId() + ".pdf")
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .body(pdf);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // ---------------- Méthodes utilitaires ----------------
    private void formaterDatesDemandes(List<DemandeConge> demandes) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (DemandeConge demande : demandes) {
            demande.setDateDebutFormatee(
                    demande.getDateDebut() != null ? demande.getDateDebut().format(dateFormatter) : "-");
            demande.setDateFinFormatee(demande.getDateFin() != null ? demande.getDateFin().format(dateFormatter) : "-");
            demande.setDateSoumissionFormatee(
                    demande.getDateSoumission() != null ? demande.getDateSoumission().format(dateTimeFormatter) : "-");
        }
    }
}
