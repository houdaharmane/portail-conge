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
import java.util.ArrayList;
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
    private CongeService congeService;

    // ==================== DASHBOARD ====================
    @GetMapping("/dashboard")
    public String showDashboard(Model model, HttpSession session) {
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (utilisateur == null) {
            return "redirect:/login";
        }

        utilisateur = utilisateurRepository.findById(utilisateur.getId())
                .orElse(utilisateur);

        int annee = Year.now().getValue();
        List<DemandeConge> conges = congeService.getCongesByUtilisateur(utilisateur);
        int congesConsommes = congeService.calculerJoursPrisConfirmes(utilisateur, annee);
        int soldeDisponible = congeService.calculerSoldeTotalDisponible(utilisateur, annee);
        int congesAnnuels = 30;

        formaterDatesDemandes(conges);

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

    // ==================== PHOTO PROFIL ====================
    @GetMapping("/photo/{id}")
    @ResponseBody
    public byte[] afficherPhoto(@PathVariable Integer id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id).orElse(null);
        if (utilisateur != null && utilisateur.getPhoto() != null) {
            return utilisateur.getPhoto();
        }
        return new byte[0];
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

        // Récupérer le responsable du département
        List<Utilisateur> responsables = utilisateurRepository.findByDepartement(personnel.getDepartement());
        if (responsables.isEmpty()) {
            model.addAttribute("errorMessage", "Aucun responsable trouvé pour votre département.");
            return "demande-conge-personnel";
        }

        Utilisateur responsable = responsables.stream()
                .filter(u -> "RESPONSABLE".equals(u.getRole()))
                .findFirst()
                .orElse(responsables.get(0));

        demandeConge.setResponsable(responsable);
        demandeConge.setDestinataireActuel(responsable.getNom() + " " + responsable.getPrenom());

        // Transfert automatique vers l’intérimaire si le responsable est en congé
        assignerInterimaireSiResponsableEnConge(demandeConge);

        demandeConge.setStatut(StatutDemande.EN_ATTENTE_RESPONSABLE);
        demandeConge.setDateSoumission(LocalDateTime.now());
        demandeCongeRepository.save(demandeConge);

        model.addAttribute("dashboardUrl", "/personnel/dashboard");
        return "ConfirmationDemande";
    }

    // ==================== DEMANDES A TRAITER ====================
    @GetMapping("/demandes-a-traiter")
    public String afficherDemandesATraiter(HttpSession session, Model model) {
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (utilisateur == null) {
            return "redirect:/login";
        }

        List<DemandeConge> demandes = new ArrayList<>();

        // Pour les responsables : récupérer les demandes du personnel
        if ("RESPONSABLE".equals(utilisateur.getRole())) {
            demandes = demandeCongeRepository
                    .findDemandesEnAttentePersonnelByDepartement(
                            utilisateur.getDepartement(),
                            StatutDemande.EN_ATTENTE_RESPONSABLE);

            // Vérifier et transférer automatiquement vers l’intérimaire si nécessaire
            for (DemandeConge demande : demandes) {
                assignerInterimaireSiResponsableEnConge(demande);
            }
        }

        // Ajouter les demandes pour lesquelles l'utilisateur est intérimaire
        List<DemandeConge> demandesPourInterimaire = demandeCongeRepository
                .findByInterimaireAndStatut(utilisateur, StatutDemande.EN_ATTENTE_RESPONSABLE);
        for (DemandeConge d : demandesPourInterimaire) {
            if (!demandes.contains(d)) {
                demandes.add(d);
            }
        }

        formaterDatesDemandes(demandes);

        model.addAttribute("demandes", demandes);
        model.addAttribute("activePage", "demandesATraiter");
        return "demandesATraiter";
    }

    // ==================== MÉTHODES UTILES ====================
    private void assignerInterimaireSiResponsableEnConge(DemandeConge demande) {
        Utilisateur responsable = demande.getResponsable();
        if (responsable == null)
            return;

        List<DemandeConge> congesResponsables = demandeCongeRepository
                .findByDemandeurAndStatutIn(
                        responsable,
                        List.of(StatutDemande.APPROUVEE_RESP, StatutDemande.APPROUVEE_DIRECTEUR));

        for (DemandeConge conge : congesResponsables) {
            if (conge.getInterimaire() != null &&
                    demande.getDateDebut() != null && demande.getDateFin() != null) {

                boolean chevauchement = !(demande.getDateFin().isBefore(conge.getDateDebut()) ||
                        demande.getDateDebut().isAfter(conge.getDateFin()));

                if (chevauchement) {
                    demande.setResponsable(conge.getInterimaire());
                    demande.setInterimaire(conge.getInterimaire());
                    demande.setDestinataireActuel(conge.getInterimaire().getNom() + " " +
                            conge.getInterimaire().getPrenom());
                    demandeCongeRepository.save(demande);
                    break;
                }
            }
        }
    }

    private void formaterDatesDemandes(List<DemandeConge> demandes) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (DemandeConge d : demandes) {
            if (d.getDateDebut() != null)
                d.setDateDebutFormatee(d.getDateDebut().format(formatter));
            if (d.getDateFin() != null)
                d.setDateFinFormatee(d.getDateFin().format(formatter));
            if (d.getDateSoumission() != null)
                d.setDateSoumissionFormatee(d.getDateSoumission().format(formatter));
        }
    }
}
