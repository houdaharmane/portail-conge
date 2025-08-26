package com.portailconge.portail_conge.controller;

import com.portailconge.portail_conge.model.DemandeConge;
import com.portailconge.portail_conge.model.Departement;
import com.portailconge.portail_conge.model.StatutDemande;
import com.portailconge.portail_conge.model.Utilisateur;
import com.portailconge.portail_conge.repository.UtilisateurRepository;
import com.portailconge.portail_conge.service.DemandeService;
import com.portailconge.portail_conge.repository.DemandeCongeRepository;
import com.portailconge.portail_conge.repository.DepartementRepository;

import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.List;

import java.io.IOException;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@Controller
public class RhController {
    @Autowired
    private DemandeService demandeService;
    @Autowired
    private DemandeCongeRepository demandeCongeRepository;

    @Autowired
    private DepartementRepository departementRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @GetMapping("/dashboard-rh")
    public String rhDashboard(HttpSession session, Model model) {
        Utilisateur rh = (Utilisateur) session.getAttribute("utilisateur");
        if (rh == null || !"RH".equals(rh.getRole())) {
            return "redirect:/login";
        }

        // Comptage mis à jour
        long demandesEnAttente = demandeCongeRepository.countByStatutIn(List.of(
                StatutDemande.EN_ATTENTE,
                StatutDemande.EN_ATTENTE_DIRECTEUR)); // inclut celles envoyées au directeur

        long demandesRefuses = demandeCongeRepository.countByStatut(StatutDemande.REFUSEE);

        long demandesValidees = demandeCongeRepository.countByStatutIn(List.of(
                StatutDemande.APPROUVEE_RH,
                StatutDemande.APPROUVEE_DIRECTEUR)); // inclut celles validées par le directeur

        // Historique
        List<StatutDemande> statutsHistoriqueRh = List.of(
                StatutDemande.APPROUVEE_RH,
                StatutDemande.APPROUVEE_DIRECTEUR,
                StatutDemande.REFUSEE,
                StatutDemande.EN_ATTENTE_DIRECTEUR);
        List<DemandeConge> demandesHistorique = demandeCongeRepository.findByStatutIn(statutsHistoriqueRh);

        // Notifications des nouvelles demandes du directeur
        List<DemandeConge> notifications = demandeCongeRepository.findByDemandeurRoleAndLuParRHFalse("DIRECTEUR");

        model.addAttribute("rh", rh);
        model.addAttribute("email", rh.getEmail());
        model.addAttribute("role", rh.getRole());
        model.addAttribute("activePage", "dashboard");

        model.addAttribute("demandesEnAttente", demandesEnAttente);
        model.addAttribute("demandesValidees", demandesValidees);
        model.addAttribute("demandesRefuses", demandesRefuses);
        model.addAttribute("historique", demandesHistorique.size());

        model.addAttribute("notifications", notifications);
        model.addAttribute("notificationsCount", notifications.size());

        return "dashboard-rh";
    }

    @GetMapping("/profil")
    public String afficherProfilRH(HttpSession session, Model model,
            @RequestParam(required = false) Boolean modification) {
        Utilisateur rh = (Utilisateur) session.getAttribute("utilisateur");

        if (rh == null || !"RH".equals(rh.getRole())) {
            return "redirect:/login";
        }

        model.addAttribute("rh", rh);
        model.addAttribute("modification", modification != null && modification);
        model.addAttribute("departements", departementRepository.findAll());

        model.addAttribute("activePage", "profil");
        return "profil";
    }

    @GetMapping("/profil/modifier")
    public String afficherFormulaireModificationProfil(HttpSession session, Model model) {
        Utilisateur rh = (Utilisateur) session.getAttribute("utilisateur");

        if (rh == null || !"RH".equals(rh.getRole())) {
            return "redirect:/login";
        }

        model.addAttribute("utilisateur", new Utilisateur());
        model.addAttribute("activePage", "profil");
        return "modifier_profil";
    }

    @PostMapping("/profil/save")
    public String enregistrerProfil(@ModelAttribute("utilisateur") Utilisateur utilisateurModifie,
            @RequestParam("imageFile") MultipartFile imageFile,
            HttpSession session, Model model) {
        Utilisateur rh = (Utilisateur) session.getAttribute("utilisateur");

        if (rh == null || !"RH".equals(rh.getRole())) {
            return "redirect:/login";
        }

        try {
            rh.setEmail(utilisateurModifie.getEmail());
            if (utilisateurModifie.getMotDePasse() != null && !utilisateurModifie.getMotDePasse().isEmpty()) {
                rh.setMotDePasse(utilisateurModifie.getMotDePasse());
            }
            rh.setNom(utilisateurModifie.getNom());
            rh.setPrenom(utilisateurModifie.getPrenom());
            rh.setTelephone(utilisateurModifie.getTelephone());
            rh.setCin(utilisateurModifie.getCin());
            rh.setSoldeConge(utilisateurModifie.getSoldeConge());

            if (utilisateurModifie.getDepartement() != null && utilisateurModifie.getDepartement().getId() != null) {
                departementRepository.findById(utilisateurModifie.getDepartement().getId())
                        .ifPresent(rh::setDepartement);
            } else {
                rh.setDepartement(null);
            }

            if (imageFile != null && !imageFile.isEmpty()) {
                try {
                    byte[] bytes = imageFile.getBytes();
                    rh.setPhoto(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                    model.addAttribute("error", "Erreur lors de la lecture de l'image.");
                    model.addAttribute("utilisateur", rh);
                    model.addAttribute("activePage", "profil");
                    return "profil";
                }
            }

            utilisateurRepository.save(rh);
            session.setAttribute("utilisateur", rh);

            return "redirect:/profil?modification=true";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors de la mise à jour du profil.");
            model.addAttribute("utilisateur", rh);
            model.addAttribute("activePage", "profil");
            return "profil";
        }
    }

    @PostMapping("/rh/personnel/ajouter")
    public String ajouterUtilisateur(
            @RequestParam String nom,
            @RequestParam String prenom,
            @RequestParam String email,
            @RequestParam("fonction") String fonction,
            @RequestParam String motDePasse,
            @RequestParam String role,
            @RequestParam Integer departementId,
            Model model) {

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(nom);
        utilisateur.setPrenom(prenom);
        utilisateur.setEmail(email);
        utilisateur.setRole(role); // Ici on affecte bien le rôle choisi
        utilisateur.setMotDePasse(motDePasse);
        departementRepository.findById(departementId).ifPresent(utilisateur::setDepartement);

        utilisateurRepository.save(utilisateur);

        return "redirect:/personnel/liste";
    }

    // Nouvelle méthode pour servir la photo
    @GetMapping("/profil/photo/{id}")
    @ResponseBody
    public byte[] getPhoto(@PathVariable("id") Integer id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id).orElse(null);
        return (utilisateur != null && utilisateur.getPhoto() != null) ? utilisateur.getPhoto() : new byte[0];
    }

    @GetMapping("/rh/conge/demande")
    public String afficherFormulaireDemande(HttpSession session, Model model) {
        Utilisateur rhSession = (Utilisateur) session.getAttribute("utilisateur");

        if (rhSession == null || !"RH".equals(rhSession.getRole())) {
            return "redirect:/login";
        }

        Utilisateur rh = utilisateurRepository.findById(rhSession.getId())
                .orElse(rhSession);

        model.addAttribute("rh", rh);
        model.addAttribute("activePage", "demandeConge");
        return "demande_conge";
    }

    @PostMapping("/rh/conge/demande")
    public String soumettreDemandeInteresse(
            @RequestParam("utilisateurId") Integer utilisateurId,
            @RequestParam("matricule") String matricule,
            @RequestParam("nomPrenom") String nomPrenom,
            @RequestParam("fonction") String fonction,
            @RequestParam("departementId") Integer departementId,
            @RequestParam("duree") String dureeStr,
            @RequestParam("dateDebut") String dateDebutStr,
            @RequestParam("dateFin") String dateFinStr,
            Model model,
            HttpSession session) {

        Utilisateur rh = utilisateurRepository.findById(utilisateurId).orElse(null);
        if (rh == null || !"RH".equalsIgnoreCase(rh.getRole())) {
            return "redirect:/login";
        }

        // Validation simple
        if (matricule == null || matricule.isEmpty() || departementId == null) {
            model.addAttribute("error", "Matricule et département obligatoires.");
            model.addAttribute("rh", rh);
            return "demande-conge"; // retourne le formulaire avec l'erreur
        }

        int duree;
        try {
            duree = Integer.parseInt(dureeStr);
            if (duree <= 0)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            model.addAttribute("error", "Durée invalide.");
            model.addAttribute("rh", rh);
            return "demande-conge";
        }

        LocalDate dateDebut;
        try {
            dateDebut = LocalDate.parse(dateDebutStr);
        } catch (Exception e) {
            model.addAttribute("error", "Date de début invalide.");
            model.addAttribute("rh", rh);
            return "demande-conge";
        }

        LocalDate dateFin;
        try {
            dateFin = LocalDate.parse(dateFinStr);
        } catch (Exception e) {
            dateFin = dateDebut.plusDays(duree - 1);
        }

        Departement departement = departementRepository.findById(departementId).orElse(null);
        if (departement == null) {
            model.addAttribute("error", "Département invalide.");
            model.addAttribute("rh", rh);
            return "demande-conge";
        }

        // Création de la demande
        DemandeConge demande = new DemandeConge();
        demande.setDemandeur(rh);
        demande.setDuree(duree);
        demande.setDateDebut(dateDebut);
        demande.setDateFin(dateFin);
        demande.setDepartement(departement);
        demande.setStatut(StatutDemande.EN_ATTENTE_DIRECTEUR);

        demandeCongeRepository.save(demande);

        // Préparation de la page de confirmation
        model.addAttribute("rh", rh);
        model.addAttribute("message", "Demande enregistrée et envoyée au directeur avec succès.");
        model.addAttribute("dashboardUrl", "/dashboard");
        System.out.println("Redirection vers ConfirmationDemande pour RH : " + rh.getNom());
        // Déterminer le dashboard en fonction du rôle
        String dashboardUrl;
        switch (rh.getRole()) {
            case "RH":
                dashboardUrl = "/dashboard-rh";
                break;
            case "RESPONSABLE":
                dashboardUrl = "/dashboard-responsable";
                break;
            case "DIRECTEUR":
                dashboardUrl = "/dashboard-directeur";
                break;
            default:
                dashboardUrl = "/login";
        }
        model.addAttribute("dashboardUrl", dashboardUrl);
        return "ConfirmationDemande";
    }

    @PostMapping("/rh/demande/valider")
    public String validerDemande(@RequestParam("id") Long id, HttpSession session) {
        Utilisateur rh = (Utilisateur) session.getAttribute("utilisateur");

        if (rh == null || !"RH".equals(rh.getRole())) {
            return "redirect:/login";
        }

        demandeCongeRepository.findById(id).ifPresent(demande -> {
            demande.setStatut(StatutDemande.APPROUVEE_RH);
            demandeCongeRepository.save(demande);
        });

        return "redirect:/rh/demandes-approuvees-responsable";
    }

    @GetMapping("/rh/refuser/{id}")
    public String refuserDemandeGet(@PathVariable Long id, HttpSession session) {
        Utilisateur rh = (Utilisateur) session.getAttribute("utilisateur");
        if (rh == null || !"RH".equals(rh.getRole())) {
            return "redirect:/login";
        }

        demandeCongeRepository.findById(id).ifPresent(demande -> {
            demande.setStatut(StatutDemande.REFUSEE);
            demandeCongeRepository.save(demande);
        });

        return "rh-demandes-responsable";
    }

    @GetMapping("/rh/historique-demandes")
    public String afficherHistoriqueRh(HttpSession session, Model model) {
        Utilisateur rh = (Utilisateur) session.getAttribute("utilisateur");

        if (rh == null || !"RH".equals(rh.getRole())) {
            return "redirect:/login";
        }

        List<StatutDemande> statutsHistoriqueRh = List.of(
                StatutDemande.APPROUVEE_RH,
                StatutDemande.REFUSEE,
                StatutDemande.EN_ATTENTE_DIRECTEUR);

        List<DemandeConge> demandesHistorique = demandeCongeRepository.findByStatutIn(statutsHistoriqueRh);

        model.addAttribute("rh", rh);
        model.addAttribute("demandes", demandesHistorique);
        model.addAttribute("activePage", "historiqueDemandesRh");

        return "historique-demandes-rh";
    }

    // Afficher la liste des utilisateurs
    @GetMapping("/rh/utilisateurs")
    public String afficherUtilisateurs(HttpSession session, Model model) {
        Utilisateur rh = (Utilisateur) session.getAttribute("utilisateur");
        if (rh == null || !"RH".equals(rh.getRole())) {
            return "redirect:/login";
        }
        List<Utilisateur> utilisateurs = utilisateurRepository.findAll();
        model.addAttribute("utilisateurs", utilisateurs);
        model.addAttribute("rh", rh);
        model.addAttribute("activePage", "utilisateurs");
        model.addAttribute("utilisateur", new Utilisateur()); // formulaire vide par défaut
        return "utilisateurs";
    }

    // Afficher la liste + formulaire prérempli pour modification
    @GetMapping("/rh/utilisateurs/modifier/{id}")
    public String afficherFormulaireModificationUtilisateur(@PathVariable("id") Integer id, Model model,
            HttpSession session) {
        Utilisateur rh = (Utilisateur) session.getAttribute("utilisateur");
        if (rh == null || !"RH".equals(rh.getRole())) {
            return "redirect:/login";
        }

        List<Utilisateur> utilisateurs = utilisateurRepository.findAll();
        Utilisateur utilisateur = utilisateurRepository.findById(id).orElse(null);

        model.addAttribute("utilisateurs", utilisateurs);
        model.addAttribute("utilisateur", utilisateur);
        model.addAttribute("rh", rh);
        model.addAttribute("activePage", "utilisateurs");

        return "utilisateurs";
    }

    // Enregistrer la modification puis retourner liste avec formulaire vide
    @PostMapping("/rh/utilisateurs/modifier")
    public String enregistrerModificationUtilisateur(@ModelAttribute("utilisateur") Utilisateur utilisateurModifie,
            HttpSession session, Model model) {
        Utilisateur rh = (Utilisateur) session.getAttribute("utilisateur");
        if (rh == null || !"RH".equals(rh.getRole())) {
            return "redirect:/login";
        }

        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurModifie.getId()).orElse(null);
        if (utilisateur != null) {
            utilisateur.setEmail(utilisateurModifie.getEmail());
            if (utilisateurModifie.getMotDePasse() != null && !utilisateurModifie.getMotDePasse().isEmpty()) {
                utilisateur.setMotDePasse(utilisateurModifie.getMotDePasse());
            }
            utilisateurRepository.save(utilisateur);
        }

        // Recharge la liste et formulaire vide après modification
        List<Utilisateur> utilisateurs = utilisateurRepository.findAll();
        model.addAttribute("utilisateurs", utilisateurs);
        model.addAttribute("utilisateur", new Utilisateur()); // formulaire vide
        model.addAttribute("rh", rh);
        model.addAttribute("activePage", "utilisateurs");

        return "utilisateurs";
    }

    @GetMapping("/rh/demandes-responsable")
    public String demandesResponsable(HttpSession session, Model model) {
        Utilisateur rh = (Utilisateur) session.getAttribute("utilisateur");

        if (rh == null || !"RH".equals(rh.getRole())) {
            return "redirect:/login";
        }

        List<DemandeConge> demandes = demandeCongeRepository.findByStatutAndDemandeurRole(StatutDemande.EN_ATTENTE,
                "RESPONSABLE");
        model.addAttribute("demandesResponsable", demandes);
        model.addAttribute("rh", rh);
        return "rh-demandes-responsable";
    }

    @GetMapping("/rh/demandes-approuvees")
    public String demandesApprouvees(HttpSession session, Model model) {
        Utilisateur rh = (Utilisateur) session.getAttribute("utilisateur");

        if (rh == null || !"RH".equals(rh.getRole())) {
            return "redirect:/login";
        }

        List<DemandeConge> demandes = demandeService.getDemandesApprouveesParResponsable();
        model.addAttribute("rh", rh);
        model.addAttribute("demandesApprouvees", demandes);
        model.addAttribute("activePage", "demandesApprouvees");

        return "demandes-approuvees";
    }

    @GetMapping("/demande-conge")
    public String demandeConge(Model model, HttpSession session) {
        Utilisateur rh = (Utilisateur) session.getAttribute("utilisateur");
        System.out.println("Utilisateur en session dans /demande-conge : " + rh);
        if (rh == null) {
            return "redirect:/login";
        }
        model.addAttribute("rh", rh);
        return "demande-conge";
    }

    @GetMapping("/rh/accepter/{id}")
    public String accepterDemande(@PathVariable Long id, HttpSession session) {
        Utilisateur rh = (Utilisateur) session.getAttribute("utilisateur");
        if (rh == null || !"RH".equals(rh.getRole())) {
            return "redirect:/login";
        }

        demandeCongeRepository.findById(id).ifPresent(demande -> {
            demande.setStatut(StatutDemande.EN_ATTENTE_DIRECTEUR);
            demandeCongeRepository.save(demande);
        });

        return "redirect:/rh/demandes-responsable";
    }

    @GetMapping("/rh/historique-validations")
    public String historiqueValidations(Model model, HttpSession session) {
        Utilisateur rh = (Utilisateur) session.getAttribute("utilisateur");
        if (rh == null || !"RH".equals(rh.getRole())) {
            return "redirect:/login";
        }

        List<DemandeConge> demandesHistorique = demandeCongeRepository.findByStatut(StatutDemande.EN_ATTENTE_DIRECTEUR);
        model.addAttribute("demandesHistorique", demandesHistorique);

        return "historique-validations-rh";
    }

    // ---------------- Liste des demandes du directeur ----------------
    @GetMapping("/rh/demandes-directeur")
    public String afficherDemandesDirecteur(Model model) {
        List<DemandeConge> demandes = demandeCongeRepository.findByDemandeurRole("DIRECTEUR");

        // Compter les demandes non lues par RH
        long nouvellesDemandes = demandes.stream()
                .filter(d -> Boolean.FALSE.equals(d.getLuParRH()))
                .count();

        // Ajouter les attributs au modèle
        model.addAttribute("demandes", demandes);
        model.addAttribute("nouvellesDemandes", nouvellesDemandes);

        return "demandes-directeur";
    }

    // ---------------- Marquer les demandes comme lues ----------------
    @GetMapping("/rh/demandes-directeur/lues")
    public String marquerDemandesCommeLues() {
        List<DemandeConge> nouvelles = demandeCongeRepository.findByDemandeurRole("DIRECTEUR")
                .stream().filter(d -> Boolean.FALSE.equals(d.getLuParRH()))
                .toList();

        nouvelles.forEach(d -> d.setLuParRH(true));
        demandeCongeRepository.saveAll(nouvelles);

        return "redirect:/rh/demandes-directeur";
    }

    @GetMapping("/rh/notifications")
    @ResponseBody
    public List<Map<String, Object>> getNotifications() {
        List<DemandeConge> nouvellesDemandes = demandeCongeRepository.findByDemandeurRole("DIRECTEUR")
                .stream()
                .filter(d -> d.getLuParRH() == null || !d.getLuParRH())
                .toList();

        return nouvellesDemandes.stream()
                .map(d -> {
                    Map<String, Object> map = new HashMap<>();
                    Map<String, String> demandeur = new HashMap<>();
                    demandeur.put("nom", d.getDemandeur().getNom());
                    demandeur.put("prenom", d.getDemandeur().getPrenom());
                    map.put("demandeur", demandeur);
                    map.put("dateDebut", d.getDateDebut() != null ? d.getDateDebut().toString() : "");
                    map.put("dateFin", d.getDateFin() != null ? d.getDateFin().toString() : "");
                    map.put("dateSoumission",
                            d.getDateSoumission() != null ? d.getDateSoumission().toLocalDate().toString() : "");
                    map.put("heureSoumission",
                            d.getDateSoumission() != null
                                    ? d.getDateSoumission().toLocalTime().withSecond(0).withNano(0).toString()
                                    : "");
                    return map;
                })
                .toList();

    }
}