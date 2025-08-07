package com.portailconge.portail_conge.service;

import com.portailconge.portail_conge.model.DemandeConge;
import com.portailconge.portail_conge.repository.DemandeCongeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;

@Service
public class DemandeService {

    @Autowired
    private DemandeCongeRepository demandeCongeRepository;

    public void save(DemandeConge demande) {
        demandeCongeRepository.save(demande);
    }

    public Page<DemandeConge> getDemandesPage(int page, int size) {
        return demandeCongeRepository.findAll(PageRequest.of(page, size));
    }
}
