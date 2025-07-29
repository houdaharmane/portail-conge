package com.portailconge.portail_conge.controller;

import com.portailconge.portail_conge.model.Departement;
import com.portailconge.portail_conge.model.Personnel;
import com.portailconge.portail_conge.model.Utilisateur;
import com.portailconge.portail_conge.repository.DepartementRepository;
import com.portailconge.portail_conge.service.AuthService;

import jakarta.servlet.http.HttpSession;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.portailconge.portail_conge.service.UtilisateurService;
import org.springframework.http.HttpHeaders;

@Controller
@RequestMapping("/personnel")
public class PersonnelController {
    @Autowired
    private UtilisateurService utilisateurService;
    @Autowired
    private DepartementRepository departementRepository;

    @Autowired
    private AuthService authService;

    @GetMapping("/ajouter")
    public String afficherFormulaireAjout(Model model, HttpSession session) {
        Utilisateur rh = (Utilisateur) session.getAttribute("utilisateur");
        if (rh == null || !"RH".equals(rh.getRole())) {
            return "redirect:/login";
        }

        model.addAttribute("rh", rh);
        model.addAttribute("departements", departementRepository.findAll());

        model.addAttribute("personnel", new Personnel());

        return "ajouter-personnel";
    }

    @PostMapping("/ajouter")
    public String ajouterPersonnel(
            @RequestParam String matricule,
            @RequestParam String nom,
            @RequestParam String prenom,
            @RequestParam("numero") String telephone,
            @RequestParam String email,
            @RequestParam String role,
            @RequestParam Integer departement,
            @RequestParam String cin,
            @RequestParam String motDePasse,
            @RequestParam Integer soldeConge,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Utilisateur rh = (Utilisateur) session.getAttribute("utilisateur");
        if (rh == null || !"RH".equals(rh.getRole())) {
            return "redirect:/login";
        }

        try {
            // Créer l'utilisateur
            Utilisateur utilisateur = new Utilisateur();
            utilisateur.setMatricule(matricule);
            utilisateur.setNom(nom);
            utilisateur.setPrenom(prenom);
            utilisateur.setTelephone(telephone);
            utilisateur.setEmail(email);
            utilisateur.setRole(role);

            Departement dept = new Departement();
            dept.setId(departement);
            utilisateur.setDepartement(dept);

            utilisateur.setCin(cin);
            utilisateur.setMotDePasse(motDePasse);
            utilisateur.setSoldeConge(soldeConge);

            // Créer le personnel
            Personnel personnel = new Personnel();
            personnel.setNom(nom);
            personnel.setPrenom(prenom);
            personnel.setMatricule(matricule);
            personnel.setTelephone(telephone);
            personnel.setEmail(email);
            personnel.setCin(cin);

            personnel.setDepartementId(departement);

            personnel.setUtilisateur(utilisateur); // liaison

            authService.creerUtilisateurEtPersonnel(utilisateur, personnel);

            redirectAttributes.addFlashAttribute("success", "Personnel ajouté avec succès !");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'ajout du personnel.");
        }

        return "redirect:/personnel/ajouter";

    }

    // Interface personnel : tableau de bord
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("activePage", "dashboard");
        return "dashboard-personnel";
    }

    // Interface personnel : profil
    @GetMapping("/profil-personnel")
    public String showProfil(HttpSession session, Model model) {
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        System.out.println("Session utilisateur : " + utilisateur);

        if (utilisateur == null) {
            System.out.println("Utilisateur null, redirection vers login");

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

    @GetMapping("/photo/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> getPhotoProfil(@PathVariable("id") int utilisateurId) {
        Utilisateur utilisateur = utilisateurService.findById(utilisateurId);
        if (utilisateur == null || utilisateur.getPhoto() == null) {
            return ResponseEntity.notFound().build();
        }
        byte[] image = utilisateur.getPhoto();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .body(image);
    }

    @GetMapping("/demande-conge-personnel")
    public String afficherFormulaireCongePersonnel(Model model, HttpSession session) {
        Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur");
        if (utilisateur == null) {
            return "redirect:/login";
        }
        String nomPrenom = utilisateur.getNom() + " " + utilisateur.getPrenom();

        model.addAttribute("matricule", utilisateur.getMatricule());
        model.addAttribute("nomPrenom", nomPrenom);

        return "demande-conge-personnel";
    }

}