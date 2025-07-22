package com.portailconge.portail_conge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.portailconge.portail_conge.model.Personnel;

public interface PersonnelRepository extends JpaRepository<Personnel, Integer> {
}
