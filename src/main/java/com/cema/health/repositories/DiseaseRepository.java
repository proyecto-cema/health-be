package com.cema.health.repositories;

import com.cema.health.entities.CemaDisease;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiseaseRepository extends JpaRepository<CemaDisease, Long> {

    CemaDisease findCemaDiseaseByNameAndEstablishmentCuigIgnoreCase(String name, String cuig);

    Page<CemaDisease> findAllByEstablishmentCuig(String cuig, Pageable paging);
}
