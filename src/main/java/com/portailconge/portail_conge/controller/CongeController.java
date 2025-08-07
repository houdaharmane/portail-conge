package com.portailconge.portail_conge.controller;

import com.portailconge.portail_conge.model.DemandeConge;
import com.portailconge.portail_conge.model.Utilisateur;
import com.portailconge.portail_conge.repository.DemandeCongeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/conge")
public class CongeController {

    @Autowired
    private DemandeCongeRepository demandeCongeRepository;

    @GetMapping("/demande")
    public String afficherFormulaireDemandeConge(Model model) {
        model.addAttribute("activePage", "demandeConge");

        // Exemple statique pour test
        Utilisateur rh = new Utilisateur();
        rh.setRole("RH");
        model.addAttribute("rh", rh);

        return "demande-conge";
    }

    @PostMapping("/etape-suivante")
    public String traiterDemandeConge(@RequestParam("dateDebut") String dateDebut,
            @RequestParam("dateFin") String dateFin,
            Model model) {
        model.addAttribute("dateDebut", dateDebut);
        model.addAttribute("dateFin", dateFin);

        return "details-demande";
    }

    @GetMapping("/historique-responsable")
    public String afficherDemandesResponsable(Model model) {
        List<DemandeConge> demandes = demandeCongeRepository.findByDemandeurRole("RESPONSABLE");
        model.addAttribute("demandes", demandes);
        return "historique-demandes-responsable";
    }

    @GetMapping("/historique-personnel")
    public String afficherDemandesPersonnel(Model model) {
        List<DemandeConge> demandes = demandeCongeRepository.findByDemandeurRole("PERSONNEL");
        model.addAttribute("demandes", demandes);
        return "historique-demandes-personnel";
    }
}
