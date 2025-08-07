package com.portailconge.portail_conge.controller;

import com.portailconge.portail_conge.model.DemandeConge;
import com.portailconge.portail_conge.model.StatutDemande;
import com.portailconge.portail_conge.model.Utilisateur;
import com.portailconge.portail_conge.repository.UtilisateurRepository;
import com.portailconge.portail_conge.repository.DemandeCongeRepository;
import com.portailconge.portail_conge.repository.DepartementRepository;

import jakarta.servlet.http.HttpSession;

import java.util.List;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class RhController {
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

        model.addAttribute("rh", rh);
        model.addAttribute("email", rh.getEmail());
        model.addAttribute("role", rh.getRole());
        model.addAttribute("activePage", "dashboard");
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
            @RequestParam String motDePasse,
            @RequestParam String role,
            @RequestParam Integer departementId,
            Model model) {

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(nom);
        utilisateur.setPrenom(prenom);
        utilisateur.setEmail(email);
        utilisateur.setMotDePasse(motDePasse);
        utilisateur.setRole(role); // Ici on affecte bien le rôle choisi

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
        Utilisateur rh = (Utilisateur) session.getAttribute("utilisateur");

        if (rh == null || !"RH".equals(rh.getRole())) {
            return "redirect:/login";
        }
        System.out.println("Utilisateur en session:");
        System.out.println("Nom = " + rh.getNom());
        System.out.println("Prénom = " + rh.getPrenom());
        System.out.println("Matricule = " + rh.getMatricule());

        model.addAttribute("rh", rh);
        model.addAttribute("activePage", "demandeConge");
        return "demande_conge";
    }

    @PostMapping("/rh/conge/demande")
    public String soumettreDemandeInteresse(
            @RequestParam("matricule") String matricule,
            @RequestParam("nomPrenom") String nomPrenom,
            @RequestParam("fonction") String fonction,
            @RequestParam("departementId") Integer departementId,
            @RequestParam("duree") String duree,
            @RequestParam("dateDebut") String dateDebut,
            @RequestParam("dateFin") String dateFin,
            Model model,
            HttpSession session) {
        Utilisateur rh = (Utilisateur) session.getAttribute("utilisateur");

        if (rh == null || !"RH".equals(rh.getRole())) {
            return "redirect:/login";
        }

        System.out.println("Demande reçue :");
        System.out.println("Matricule : " + matricule);
        System.out.println("Nom Prénom : " + nomPrenom);
        System.out.println("Fonction : " + fonction);
        System.out.println("Département ID : " + departementId);
        System.out.println("Durée : " + duree);
        System.out.println("Du " + dateDebut + " au " + dateFin);

        return "redirect:/ConfirmationDemande";
    }

    @GetMapping("/rh/demandes-approuvees-responsable")
    public String afficherDemandesApprouveesParResponsable(HttpSession session, Model model) {
        Utilisateur rh = (Utilisateur) session.getAttribute("utilisateur");

        if (rh == null || !"RH".equals(rh.getRole())) {
            return "redirect:/login";
        }

        // Demandes approuvées par le responsable
        List<DemandeConge> demandesApprouvees = demandeCongeRepository.findByStatut(StatutDemande.APPROUVEE_RESP);

        // Demandes réalisées par des responsables
        List<DemandeConge> demandesRealiseesParResponsable = demandeCongeRepository.findByDemandeurRole("RESPONSABLE");

        // Envoie les deux listes à la vue
        model.addAttribute("demandesApprouvees", demandesApprouvees);
        model.addAttribute("demandesRealisees", demandesRealiseesParResponsable);

        model.addAttribute("rh", rh);
        model.addAttribute("activePage", "demandesApprouveesParResponsable");

        return "demandeApprouveesRH";
    }

    @PostMapping("/rh/demande/valider")
    public String validerDemande(@RequestParam("id") Long id, HttpSession session) {
        Utilisateur rh = (Utilisateur) session.getAttribute("utilisateur");

        if (rh == null || !"RH".equals(rh.getRole())) {
            return "redirect:/login";
        }

        demandeCongeRepository.findById(id).ifPresent(demande -> {
            demande.setStatut(StatutDemande.APPROUVEE_RH); // ou VALIDEE selon ta logique
            demandeCongeRepository.save(demande);
        });

        return "redirect:/rh/demandes-approuvees-responsable";
    }

    @PostMapping("/rh/demande/refuser")
    public String refuserDemande(@RequestParam("id") Long id, HttpSession session) {
        Utilisateur rh = (Utilisateur) session.getAttribute("utilisateur");

        if (rh == null || !"RH".equals(rh.getRole())) {
            return "redirect:/login";
        }

        demandeCongeRepository.findById(id).ifPresent(demande -> {
            demande.setStatut(StatutDemande.REFUSEE);
            demandeCongeRepository.save(demande);
        });

        return "redirect:/rh/demandes-approuvees-responsable";
    }

    @GetMapping("/rh/historique-demandes")
    public String afficherHistoriqueRh(HttpSession session, Model model) {
        Utilisateur rh = (Utilisateur) session.getAttribute("utilisateur");

        if (rh == null || !"RH".equals(rh.getRole())) {
            return "redirect:/login";
        }

        // Statuts pertinents pour RH (par exemple, toutes les demandes
        // approuvées/refusées par RH)
        List<StatutDemande> statutsHistoriqueRh = List.of(
                StatutDemande.APPROUVEE_RH,
                StatutDemande.REFUSEE);

        List<DemandeConge> demandesHistorique = demandeCongeRepository.findByStatutIn(statutsHistoriqueRh);

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
        model.addAttribute("utilisateur", utilisateur); // formulaire prérempli
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

}
