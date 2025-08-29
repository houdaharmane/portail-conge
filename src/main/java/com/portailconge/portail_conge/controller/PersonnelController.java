package com.portailconge.portail_conge.controller;

import com.portailconge.portail_conge.model.DemandeConge;
import com.portailconge.portail_conge.model.StatutDemande;
import com.portailconge.portail_conge.model.Utilisateur;
import com.portailconge.portail_conge.repository.DemandeCongeRepository;
import com.portailconge.portail_conge.repository.UtilisateurRepository;
import com.portailconge.portail_conge.service.UtilisateurService;
import com.portailconge.portail_conge.service.CongeService;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/personnel")
public class PersonnelController {

    @Autowired
    private DemandeCongeRepository demandeCongeRepository;

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private CongeService congeService; // Pour DemandeConge

    // ==================== DASHBOARD ====================
    @GetMapping("/dashboard")
    public String showDashboard(Model model, HttpSession session) {
        // Récupérer l'utilisateur depuis la session
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (utilisateur == null) {
            return "redirect:/login";
        }

        // Recharger l'utilisateur depuis la base
        utilisateur = utilisateurRepository.findById(utilisateur.getId())
                .orElse(utilisateur);

        int annee = Year.now().getValue();

        // Récupérer les congés via le service (DemandeConge)
        List<DemandeConge> conges = congeService.getCongesByUtilisateur(utilisateur);

        // Calculer les jours pris confirmés et le solde
        int congesConsommes = congeService.calculerJoursPrisConfirmes(utilisateur, annee);
        int soldeDisponible = congeService.calculerSoldeTotalDisponible(utilisateur, annee);
        int congesAnnuels = 30;

        // Formater les dates pour affichage
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (DemandeConge d : conges) {
            if (d.getDateDebut() != null)
                d.setDateDebutFormatee(d.getDateDebut().format(dateFormatter));
            if (d.getDateFin() != null)
                d.setDateFinFormatee(d.getDateFin().format(dateFormatter));
            if (d.getDateSoumission() != null)
                d.setDateSoumissionFormatee(d.getDateSoumission().format(dateTimeFormatter));
        }

        // Ajouter les attributs au modèle
        model.addAttribute("utilisateur", utilisateur);
        model.addAttribute("conges", conges);
        model.addAttribute("congesAnnuels", congesAnnuels);
        model.addAttribute("congesConsommes", congesConsommes);
        model.addAttribute("soldeTotal", soldeDisponible);
        model.addAttribute("activePage", "dashboard");

        return "dashboard-personnel";
    }

    // ==================== PROFIL ====================
    @GetMapping("/profil-personnel")
    public String showProfil(HttpSession session, Model model) {
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (utilisateur == null) {
            return "redirect:/login";
        }
        model.addAttribute("activePage", "profil");
        session.setAttribute("utilisateur", utilisateur);
        model.addAttribute("utilisateur", utilisateur);
        model.addAttribute("personnelrole", utilisateur.getRole());
        return "profil-personnel";
    }

    @PostMapping("/modifier")
    public String modifierProfil(
            @RequestParam String prenom,
            @RequestParam String nom,
            @RequestParam String cin,
            @RequestParam String telephone,
            @RequestParam String email,
            @RequestParam(value = "photo", required = false) MultipartFile imageFile,
            Model model,
            HttpSession session) {

        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (utilisateur == null) {
            model.addAttribute("errorMessage", "Utilisateur non connecté.");
            return "redirect:/login";
        }

        utilisateur.setPrenom(prenom);
        utilisateur.setNom(nom);
        utilisateur.setCin(cin);
        utilisateur.setTelephone(telephone);
        utilisateur.setEmail(email);

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                utilisateur.setPhoto(imageFile.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
                model.addAttribute("errorMessage", "Erreur lors du chargement de l'image.");
                model.addAttribute("utilisateur", utilisateur);
                return "profil-personnel";
            }
        }

        utilisateurService.save(utilisateur);
        session.setAttribute("utilisateur", utilisateur);
        model.addAttribute("utilisateur", utilisateur);
        model.addAttribute("successMessage", "Profil modifié avec succès !");
        return "profil-personnel";
    }

    // ==================== DEMANDE CONGE ====================
    @GetMapping("/demande-conge-personnel")
    public String afficherFormulaireCongePersonnel(Model model, HttpSession session) {
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (utilisateur == null) {
            return "redirect:/login";
        }
        String nomPrenom = utilisateur.getNom() + " " + utilisateur.getPrenom();

        model.addAttribute("matricule", utilisateur.getMatricule());
        model.addAttribute("nomPrenom", nomPrenom);
        model.addAttribute("role", utilisateur.getRole());
        model.addAttribute("departementId", utilisateur.getDepartement().getId());

        return "demande-conge-personnel";
    }

    @PostMapping("/conge/demande/soumettre")
    public String soumettreDemandeCongePersonnel(@ModelAttribute DemandeConge demandeConge,
            HttpSession session,
            Model model) {
        Utilisateur personnel = (Utilisateur) session.getAttribute("utilisateur");
        if (personnel == null || !"PERSONNEL".equals(personnel.getRole())) {
            return "redirect:/login";
        }

        demandeConge.setDemandeur(personnel);
        demandeConge.setStatut(StatutDemande.EN_ATTENTE_RESPONSABLE);
        demandeConge.setDateSoumission(LocalDateTime.now());
        demandeCongeRepository.save(demandeConge);

        model.addAttribute("dashboardUrl", "/personnel/dashboard");
        return "ConfirmationDemande";
    }

    @GetMapping("/photo/{id}")
    @ResponseBody
    public byte[] afficherPhoto(@PathVariable Integer id) { // <-- Integer ici
        Utilisateur utilisateur = utilisateurRepository.findById(id).orElse(null);
        if (utilisateur != null && utilisateur.getPhoto() != null) {
            return utilisateur.getPhoto();
        }
        return new byte[0];
    }

}
