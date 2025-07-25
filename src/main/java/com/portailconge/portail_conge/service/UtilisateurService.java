package com.portailconge.portail_conge.service;

import com.portailconge.portail_conge.model.Utilisateur;
import com.portailconge.portail_conge.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UtilisateurService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    public Utilisateur findById(int id) {
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findById(id);
        return utilisateurOpt.orElse(null);
    }

    public void save(Utilisateur utilisateur) {
        utilisateurRepository.save(utilisateur);
    }

}
