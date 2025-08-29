package com.portailconge.portail_conge.controller;

import com.portailconge.portail_conge.DTO.UtilisateurDTO;
import com.portailconge.portail_conge.model.Utilisateur;
import com.portailconge.portail_conge.model.Departement;
import com.portailconge.portail_conge.model.Personnel;
import com.portailconge.portail_conge.service.AuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UtilisateurDTO dto) {
        // Création de l'objet Utilisateur
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setMatricule(dto.getMatricule());
        utilisateur.setNom(dto.getNom());
        utilisateur.setPrenom(dto.getPrenom());
        utilisateur.setTelephone(dto.getTelephone());
        utilisateur.setEmail(dto.getEmail());
        utilisateur.setRole(dto.getRole());

        Departement dept = new Departement();
        dept.setId(dto.getDepartement());
        utilisateur.setDepartement(dept);

        utilisateur.setCin(dto.getCin());
        utilisateur.setMotDePasse(dto.getMotDePasse());
        utilisateur.setSoldeConge(dto.getSoldeConge());

        Personnel personnel = new Personnel();
        personnel.setNom(dto.getNom());
        personnel.setPrenom(dto.getPrenom());

        authService.creerUtilisateurEtPersonnel(utilisateur, personnel);

        return ResponseEntity.ok("Utilisateur et personnel créés avec succès");
    }
}
