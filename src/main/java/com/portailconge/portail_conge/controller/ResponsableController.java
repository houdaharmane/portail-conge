package com.portailconge.portail_conge.controller;

import com.portailconge.portail_conge.model.CongePdfGenerator;
import com.portailconge.portail_conge.model.DemandeConge;
import com.portailconge.portail_conge.model.Departement;
import com.portailconge.portail_conge.model.StatutDemande;
import com.portailconge.portail_conge.model.Utilisateur;
import com.portailconge.portail_conge.repository.DemandeCongeRepository;
import com.portailconge.portail_conge.repository.DepartementRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.io.IOException;
import org.springframework.http.HttpHeaders;

@Controller
public class ResponsableController {
    @Autowired
    private CongePdfGenerator congePdfGenerator;
    @Autowired
    private DepartementRepository departementRepository;
    @Autowired
    private DemandeCongeRepository demandeCongeRepository;

    // Dashboard responsable
    @GetMapping("/dashboard-responsable")
    public String dashboardResponsable(HttpSession session, Model model) {
        Utilisateur responsable = (Utilisateur) session.getAttribute("utilisateur");
        if (responsable == null)
            return "redirect:/login";

        if (!("RESPONSABLE".equals(responsable.getRole()) || "PERSONNEL".equals(responsable.getRole()))) {
            return "redirect:/login";
        }

        int congesEnAttente = demandeCongeRepository
                .countByStatutAndDemandeur_Departement(StatutDemande.EN_ATTENTE_RESPONSABLE,
                        responsable.getDepartement());

        int congesValides = demandeCongeRepository
                .countByStatutAndDemandeur_Departement(StatutDemande.APPROUVEE_RESP, responsable.getDepartement());

        int congesRefuses = demandeCongeRepository
                .countByStatutAndDemandeur_Departement(StatutDemande.REFUSEE_RESP, responsable.getDepartement());

        model.addAttribute("congesEnAttente", congesEnAttente);
        model.addAttribute("congesValides", congesValides);
        model.addAttribute("congesRefuses", congesRefuses);
        model.addAttribute("utilisateur", responsable);

        return "dashboard-responsable";
    }

    // Soumission demande (personnel)
    @PostMapping("/conge/demande/soumettre")
    public String soumettreDemande(@ModelAttribute DemandeConge demandeConge, HttpSession session) {
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (utilisateur == null)
            return "redirect:/login";

        demandeConge.setDemandeur(utilisateur);
        demandeConge.setStatut(StatutDemande.EN_ATTENTE);
        demandeConge.setDateSoumission(LocalDateTime.now());
        demandeCongeRepository.save(demandeConge);

        return "redirect:/conges/confirmation";
    }

    // Afficher demandes à traiter
    @GetMapping("/demandes-a-traiter")
    public String afficherDemandesATraiter(HttpSession session, Model model) {
        Utilisateur responsable = (Utilisateur) session.getAttribute("utilisateur");
        if (responsable == null || !"RESPONSABLE".equals(responsable.getRole()))
            return "redirect:/login";

        Departement departement = responsable.getDepartement();
        List<DemandeConge> demandes = demandeCongeRepository.findDemandesEnAttentePersonnelByDepartement(
                departement, StatutDemande.EN_ATTENTE_RESPONSABLE);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (DemandeConge demande : demandes) {
            if (demande.getDateDebut() != null)
                demande.setDateDebutFormatee(demande.getDateDebut().format(formatter));
            if (demande.getDateFin() != null)
                demande.setDateFinFormatee(demande.getDateFin().format(formatter));
        }

        model.addAttribute("demandes", demandes);
        model.addAttribute("activePage", "demandesATraiter");

        return "demandesATraiter";
    }

    // Approuver demande responsable
    @GetMapping("/approuver/{id}")
    public String approuverDemande(@PathVariable Long id, HttpSession session) {
        Utilisateur responsable = (Utilisateur) session.getAttribute("utilisateur");
        if (responsable == null || !"RESPONSABLE".equals(responsable.getRole()))
            return "redirect:/login";

        DemandeConge demande = demandeCongeRepository.findById(id).orElse(null);
        if (demande != null) {
            demande.setStatut(StatutDemande.APPROUVEE_RESP);
            demandeCongeRepository.save(demande);
        }
        return "redirect:/demandes-a-traiter";
    }

    // Refuser demande responsable
    @GetMapping("/rejeter/{id}")
    public String rejeterDemande(@PathVariable Long id, HttpSession session) {
        Utilisateur responsable = (Utilisateur) session.getAttribute("utilisateur");
        if (responsable == null || !"RESPONSABLE".equals(responsable.getRole()))
            return "redirect:/login";

        DemandeConge demande = demandeCongeRepository.findById(id).orElse(null);
        if (demande != null) {
            demande.setStatut(StatutDemande.REFUSEE_RESP);
            demandeCongeRepository.save(demande);
        }
        return "redirect:/demandes-a-traiter";
    }

    // Formulaire demande congé responsable
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
        demandeConge.setDateSoumission(LocalDateTime.now());

        demandeCongeRepository.save(demandeConge);

        String dashboardUrl = "/dashboard-responsable";
        model.addAttribute("dashboardUrl", dashboardUrl);

        return "ConfirmationDemande";
    }

    // Historique demandes responsable
    @GetMapping("/responsable/historique-demandes-responsable")
    public String afficherHistoriqueDemandesResponsable(HttpSession session, Model model,
            @RequestParam(defaultValue = "0") int page) {

        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (utilisateur == null || !"RESPONSABLE".equals(utilisateur.getRole()))
            return "redirect:/login";

        int pageSize = 3;
        Pageable pageable = PageRequest.of(page, pageSize);

        List<StatutDemande> statutsHistorique = List.of(
                StatutDemande.EN_ATTENTE, StatutDemande.EN_ATTENTE_RESPONSABLE,
                StatutDemande.APPROUVEE_RESP, StatutDemande.REFUSEE_RESP,
                StatutDemande.EN_ATTENTE_RH, StatutDemande.APPROUVEE_RH, StatutDemande.REFUSEE_RH,
                StatutDemande.EN_ATTENTE_DIRECTEUR, StatutDemande.APPROUVEE_DIRECTEUR, StatutDemande.REFUSEE_DIRECTEUR);

        Page<DemandeConge> demandesPage = demandeCongeRepository.findByDemandeurAndStatutIn(utilisateur,
                statutsHistorique, pageable);
        List<Departement> departements = departementRepository.findAll(); // récupère tous les départements

        formaterDatesDemandes(demandesPage);
        model.addAttribute("departements", departements); // ajoute la liste des départements au modèle
        model.addAttribute("demandes", demandesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", demandesPage.getTotalPages());
        model.addAttribute("activePage", "historiqueDemandesResponsable");

        return "HistoriqueDemandes-responsable";
    }

    // Historique demandes personnel
    @GetMapping("/responsable/historique-demandes-personnel")
    public String afficherHistoriqueDemandesPersonnel(HttpSession session, Model model,
            @RequestParam(defaultValue = "0") int page) {

        int pageSize = 3;
        Pageable pageable = PageRequest.of(page, pageSize);

        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (utilisateur == null || !"RESPONSABLE".equals(utilisateur.getRole()))
            return "redirect:/login";

        Page<DemandeConge> demandesPage = demandeCongeRepository.findByDemandeurRole("PERSONNEL", pageable);

        formaterDatesDemandes(demandesPage);

        model.addAttribute("demandes", demandesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", demandesPage.getTotalPages());
        model.addAttribute("activePage", "historiqueDemandesPersonnel");

        return "historique-demandes-responsable";
    }

    // Profil responsable
    @GetMapping("/profil-responsable")
    public String afficherProfil(HttpSession session, Model model) {
        Utilisateur responsable = (Utilisateur) session.getAttribute("utilisateur");
        if (responsable == null)
            return "redirect:/login";
        model.addAttribute("utilisateur", responsable);
        return "profil-responsable";
    }

    /**
     * Formate les dates pour chaque demande afin de les afficher correctement dans
     * la vue
     */
    private void formaterDatesDemandes(Page<DemandeConge> demandesPage) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (DemandeConge demande : demandesPage.getContent()) {
            demande.setDateDebutFormatee(
                    demande.getDateDebut() != null ? demande.getDateDebut().format(dateFormatter) : "-");
            demande.setDateFinFormatee(
                    demande.getDateFin() != null ? demande.getDateFin().format(dateFormatter) : "-");
            demande.setDateSoumissionFormatee(
                    demande.getDateSoumission() != null ? demande.getDateSoumission().format(dateTimeFormatter) : "-");
        }
    }

    // Générer la fiche PDF d'une demande côté responsable
    @GetMapping("/responsable/demande/{id}/fiche-pdf")
    public ResponseEntity<byte[]> genererFichePdfResponsable(@PathVariable("id") Long demandeId,
            HttpSession session) throws IOException {

        Utilisateur responsable = (Utilisateur) session.getAttribute("utilisateur");
        if (responsable == null || !"RESPONSABLE".equals(responsable.getRole()))
            return ResponseEntity.status(403).build();

        DemandeConge demande = demandeCongeRepository.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée"));

        // Ici tu peux autoriser seulement les demandes du département du responsable
        if (!demande.getDemandeur().getDepartement().equals(responsable.getDepartement())) {
            return ResponseEntity.status(403).build();
        }

        byte[] pdf;
        try {
            // Générer le PDF avec le demandeur comme argument
            pdf = congePdfGenerator.genererFichePdf(demande, demande.getDemandeur());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la génération du PDF");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=FicheConge_" + demandeId + ".pdf")
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .body(pdf);
    }
}
