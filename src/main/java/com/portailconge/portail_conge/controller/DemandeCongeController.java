package com.portailconge.portail_conge.controller;

import com.portailconge.portail_conge.model.DemandeConge;
import com.portailconge.portail_conge.model.Departement;
import com.portailconge.portail_conge.model.PdfGenerator;
import com.portailconge.portail_conge.model.StatutDemande;
import com.portailconge.portail_conge.model.Utilisateur;
import com.portailconge.portail_conge.repository.DemandeCongeRepository;
import com.portailconge.portail_conge.repository.DepartementRepository;
import com.portailconge.portail_conge.repository.UtilisateurRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@Controller
@RequestMapping("/personnel/conges")
public class DemandeCongeController {

    @Autowired
    private DemandeCongeRepository demandeCongeRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private DepartementRepository departementRepository;

    @PostMapping("/demande")
    public String enregistrerDemande(
            @RequestParam String matricule,
            @RequestParam int duree,
            @RequestParam @DateTimeFormat(pattern = "d/M/yyyy") LocalDate dateDebut,
            @RequestParam Integer departementId,
            @RequestParam(required = false) String motif,
            Model model) {

        // Vérification utilisateur par matricule
        Utilisateur demandeur = utilisateurRepository.findByMatricule(matricule);
        if (demandeur == null) {
            model.addAttribute("error", "Utilisateur non trouvé");
            return "demande-conge-personnel";
        }

        // Vérification département
        Departement departement = departementRepository.findById(departementId).orElse(null);
        if (departement == null) {
            model.addAttribute("error", "Département invalide");
            return "demande-conge-personnel";
        }

        if (duree <= 0) {
            model.addAttribute("error", "La durée doit être supérieure à zéro");
            return "demande-conge-personnel";
        }

        if (dateDebut == null) {
            model.addAttribute("error", "La date de début est obligatoire");
            return "demande-conge-personnel";
        }

        // Création et enregistrement de la demande
        DemandeConge demande = new DemandeConge();
        demande.setDemandeur(demandeur);
        demande.setDuree(duree);
        demande.setDateDebut(dateDebut);
        demande.setDepartement(departement);
        // demande.setStatut(StatutDemande.EN_ATTENTE);

        // Calcul date fin (inclus le jour de début)
        LocalDate dateFin = dateDebut.plusDays(duree - 1);
        demande.setDateFin(dateFin);
        String role = demandeur.getRole(); // Récupère le rôle de l'utilisateur

        if ("RESPONSABLE".equalsIgnoreCase(role)) {
            demande.setStatut(StatutDemande.EN_ATTENTE_RH);
        } else {
            demande.setStatut(StatutDemande.EN_ATTENTE_RESPONSABLE);
        }
        model.addAttribute("dashboardUrl", "/personnel/conges/mes-demandes");

        demandeCongeRepository.save(demande);

        model.addAttribute("message", "Demande enregistrée avec succès.");
        return "confirmationDemande";
    }

    @GetMapping("/mes-demandes")
    public String afficherMesDemandes(Model model, HttpSession session) {
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (utilisateur == null) {
            model.addAttribute("error", "Vous devez être connecté pour voir vos demandes.");
            return "login";
        }

        List<DemandeConge> mesDemandes = demandeCongeRepository.findByDemandeur(utilisateur);
        model.addAttribute("mesDemandes", mesDemandes);

        return "Demandes-personnel";
    }

    @GetMapping("/demande/{id}/titre-conge")
    public ResponseEntity<byte[]> genererTitreConge(@PathVariable Long id) {
        DemandeConge demande = demandeCongeRepository.findById(id).orElse(null);
        if (demande == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            byte[] pdfBytes = PdfGenerator.generateCongePdf(demande, "Titre-de-Congé");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "Titre-de-Congé.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

}
