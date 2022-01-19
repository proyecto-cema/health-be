package com.cema.health.mapping.impl;

import com.cema.health.domain.Disease;
import com.cema.health.entities.CemaDisease;
import com.cema.health.mapping.Mapping;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DiseaseMappingImpl implements Mapping<CemaDisease, Disease> {
    @Override
    public Disease mapEntityToDomain(CemaDisease cemaDisease) {
        return Disease.builder()
                .description(cemaDisease.getDescription())
                .duration(cemaDisease.getDuration())
                .establishmentCuig(cemaDisease.getEstablishmentCuig())
                .name(cemaDisease.getName())
                .build();
    }

    @Override
    public CemaDisease mapDomainToEntity(Disease disease) {
        return CemaDisease.builder()
                .description(disease.getDescription())
                .duration(disease.getDuration())
                .name(disease.getName())
                .establishmentCuig(disease.getEstablishmentCuig())
                .build();
    }

    @Override
    public CemaDisease updateDomainWithEntity(Disease disease, CemaDisease cemaDisease) {
        String description = StringUtils.hasText(disease.getDescription()) ? disease.getDescription() : cemaDisease.getDescription();
        Long duration = disease.getDuration() != null ? disease.getDuration() : cemaDisease.getDuration();

        cemaDisease.setDescription(description);
        cemaDisease.setDuration(duration);
        return cemaDisease;
    }
}
