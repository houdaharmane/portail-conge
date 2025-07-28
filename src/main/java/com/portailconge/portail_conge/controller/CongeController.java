package com.portailconge.portail_conge.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.portailconge.portail_conge.model.Utilisateur;

import org.springframework.ui.Model;

@Controller
@RequestMapping("/conge")
public class CongeController {

    @GetMapping("/demande")
    public String afficherFormulaireDemandeConge(Model model) {
        model.addAttribute("activePage", "demandeConge");

        // Exemple statique pour le test
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
        return "details-demande"; // prochaine étape
    }

}
