package com.cema.health.repositories;

import com.cema.health.entities.CemaIllness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IllnessRepository extends JpaRepository<CemaIllness, UUID> {

    @Query("select ci from CemaIllness ci where ci.disease.name = ?1 and ci.bovineTag = ?2 and ci.establishmentCuig = ?3")
    List<CemaIllness> findCemaIllnessByDiseaseNameAndBovineTagAndEstablishmentCuig(String diseaseName, String bovineTag, String establishmentCuig);

    Page<CemaIllness> findAllByEstablishmentCuig(String cuig, Pageable paging);

    @Query("select ci from CemaIllness ci where ci.disease.name = ?1")
    List<CemaIllness> findCemaIllnessByDiseaseName(String diseaseName);

}
